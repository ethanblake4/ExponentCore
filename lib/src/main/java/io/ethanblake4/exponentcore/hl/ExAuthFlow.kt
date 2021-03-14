package io.ethanblake4.exponentcore.hl

import android.app.Activity
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.annotation.Keep
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import io.ethanblake4.exponentcore.R
import io.ethanblake4.exponentcore.hl.mfa.AutoMFA
import io.ethanblake4.exponentcore.hl.util.LoginJsInterface
import io.ethanblake4.exponentcore.model.error.NeedsBrowserException

@Keep
object ExAuthFlow {

    private const val WEBAUTH_URL =
            "https://accounts.google.com/signin/v2/identifier?" +
                    "sacu=1&flowName=GlifWebSignIn&flowEntry=AddSession"

    private const val EMBEDDED_URL =
            "https://accounts.google.com/EmbeddedSetup/identifier?flowName=EmbeddedSetupAndroid"

    class UnspecifiedAuthError : Exception()

    enum class Format {
        WEB_AUTH,
        STANDARD,
        GOOGLE
    }

    enum class State {
        ENTER_CREDENTIALS,
        AUTHORIZE,
        ACQUIRE_MFA,
        AUTH_MFA,
        COMPLETE
    }

    @Keep
    @JvmStatic
    fun start(context: Activity, format: Format, stateCallback: (State) -> Unit,
              errorCallback: (Throwable) -> Unit, enableMFA: Boolean = true) {
        val dialog = Dialog(context)
        var state = 0
        when (format) {
            Format.WEB_AUTH -> {
                dialog.setContentView(R.layout.web_dialog)
                val webView = dialog.findViewById<WebView>(R.id.ex_auth_webview)

                // Won't work without JS!
                webView.settings.javaScriptEnabled = true
                webView.settings.displayZoomControls = false
                webView.settings.useWideViewPort = false
                webView.settings.loadWithOverviewMode = true

                // The JavaScript interface will receive credentials from the page
                webView.addJavascriptInterface(LoginJsInterface { username, pass ->

                    if (state == 1) {
                        return@LoginJsInterface
                    } else {
                        state = 1
                    }
                    // We've received the credentials from the login dialog
                    stateCallback(State.AUTHORIZE)

                    // Google login pages accept the username without suffix,
                    // but Exponent expects the suffix to be present
                    val addr = if (username.contains('@')) username else "$username@gmail.com"

                    // Try to login
                    ExponentAccounts.login(addr, pass, { _ ->
                        // Login successful!
                        stateCallback(State.COMPLETE)
                        dialog.dismiss()
                    },
                            { err ->

                                // Do we need to continue with MFA?
                                if (err is NeedsBrowserException && enableMFA)

                                // Let AutoMFA do the heavy lifting here
                                    AutoMFA.handleBrowserRecover(context, addr, err, {
                                        stateCallback(State.COMPLETE)
                                        dialog.dismiss()
                                    }, { err2 ->
                                        errorCallback(err2 ?: UnspecifiedAuthError())
                                        dialog.dismiss()
                                    }, null, webView)
                                else {
                                    errorCallback(err ?: UnspecifiedAuthError())
                                    dialog.dismiss()
                                }
                            })
                }, "Android")
                // Load the Google login page. "sacu=1" forces direct email/pass entry
                webView.loadUrl(WEBAUTH_URL)

                // Wait for the page to load
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        Log.d("WHattt!!", "Injecting for URL $url")
                        super.onPageFinished(view, url)
                        val input = context.assets.open("authflow.js")
                        val buffer = ByteArray(input.available())
                        //noinspection ResultOfMethodCallIgnored
                        input.read(buffer)
                        input.close()

                        val js = String(buffer)
                        Log.d("ExJs", js)

                        // Inject our script that 'steals' credentials from the page
                        view.evaluateJavascript(js, {})
                    }
                }
                dialog.show()
            }
            Format.GOOGLE -> {
                dialog.window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT));

                dialog.setContentView(R.layout.web_dialog)
                val webView = dialog.findViewById<WebView>(R.id.ex_auth_webview)

                // Won't work without JS!
                webView.settings.javaScriptEnabled = true
                webView.settings.displayZoomControls = false
                webView.settings.useWideViewPort = false
                webView.settings.loadWithOverviewMode = true
                webView.settings.userAgentString = "${webView.settings.userAgentString} MinuteMaid"

                CookieManager.getInstance().acceptThirdPartyCookies(webView)

                // The JavaScript interface will receive credentials from the page
                webView.addJavascriptInterface(LoginJsInterface { _, _ -> }, "Android")
                // Load the embedded Google login page.
                webView.loadUrl(EMBEDDED_URL)

                // Wait for the page to load
                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {

                        if(Uri.parse(view.url).fragment == "close") {
                            val cookies = CookieManager.getInstance().getCookie("https://accounts.google.com")

                            Log.d("TOKENS", cookies)

                            var theToken = ""

                            if (cookies.contains("oauth_token=")) {
                                cookies.split(";").map {
                                    Pair(it.split("=")[0], it.split("=")[1])
                                }.firstOrNull { it.first.contains("oauth_token") }?.second?.trim()?.let { c ->
                                    theToken = c
                                }
                            } else {
                                errorCallback(UnspecifiedAuthError())
                                return
                            }

                            Log.d("TOKEN cap", theToken)

                            ExponentAccounts.loginEmbedded(theToken, { _ ->

                                // Login successful!
                                stateCallback(State.COMPLETE)

                            },
                                    { err ->

                                       /* // Do we need to continue with MFA?
                                        if (err is NeedsBrowserException && enableMFA)

                                        // Let AutoMFA do the heavy lifting here
                                            AutoMFA.handleBrowserRecover(context, addr, err, {
                                                stateCallback(State.COMPLETE)
                                                dialog.dismiss()
                                            }, { err2 ->
                                                errorCallback(err2 ?: UnspecifiedAuthError())
                                                dialog.dismiss()
                                            }, null, webView)
                                        else {
                                            errorCallback(err ?: UnspecifiedAuthError())
                                            dialog.dismiss()
                                        }*/
                                    })

                            dialog.dismiss()

                        }

                        Log.d("ExponentInjector", "Injecting for URL $url")
                        super.onPageFinished(view, url)
                        val input = context.assets.open("authflowEmbedded.js")
                        val buffer = ByteArray(input.available())
                        //noinspection ResultOfMethodCallIgnored
                        input.read(buffer)
                        input.close()

                        val js = String(buffer)
                        Log.d("ExJs", js)

                        // Inject our script that 'steals' credentials from the page
                        view.evaluateJavascript(js) {}
                    }
                }
                dialog.show()

            }
            Format.STANDARD -> TODO("Not yet supported")
        }
        stateCallback(State.ENTER_CREDENTIALS)

    }
}