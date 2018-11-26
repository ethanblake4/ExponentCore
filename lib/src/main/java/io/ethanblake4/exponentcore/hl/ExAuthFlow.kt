package io.ethanblake4.exponentcore.hl

import android.app.Dialog
import android.content.Context
import android.support.annotation.Keep
import android.webkit.WebView
import android.webkit.WebViewClient
import io.ethanblake4.exponentcore.R
import io.ethanblake4.exponentcore.hl.mfa.AutoMFA
import io.ethanblake4.exponentcore.hl.util.LoginJsInterface
import io.ethanblake4.exponentcore.model.error.NeedsBrowserException

@Keep
object ExAuthFlow {

    class UnspecifiedAuthError: Exception()

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
    @JvmStatic fun start(context: Context, format: Format, stateCallback: (State) -> Unit,
                         errorCallback: (Throwable) -> Unit, enableMFA: Boolean = true) {
        val dialog = Dialog(context)
        when(format) {
            Format.WEB_AUTH -> {
                dialog.setContentView(R.layout.web_dialog)
                val webView = dialog.findViewById<WebView>(R.id.ex_auth_webview)

                // Won't work without JS!
                webView.settings.javaScriptEnabled = true
                webView.settings.displayZoomControls = false
                webView.settings.useWideViewPort = false
                webView.settings.loadWithOverviewMode = true

                webView.addJavascriptInterface(LoginJsInterface { username, pass ->
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
                        if(err is NeedsBrowserException && enableMFA)

                            // Let AutoMFA do the heavy lifting here
                            AutoMFA.handleBrowserRecover(context, addr, err, {
                                stateCallback(State.COMPLETE)
                                dialog.dismiss()
                            }, {
                                err2 -> errorCallback(err2 ?: UnspecifiedAuthError() )
                                dialog.dismiss()
                            }, null, webView)
                        else {
                            errorCallback(err ?: UnspecifiedAuthError())
                            dialog.dismiss()
                        }
                    })
                }, "Android")
                // Load the Google login page. "sacu=1" forces direct email/pass entry
                webView.loadUrl("https://accounts.google.com/AddSession?sacu=1")

                // Wait for the page to load
                webView.webViewClient = object: WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        val input = context.assets.open("authflow.js")
                        val buffer = ByteArray(input.available())
                        //noinspection ResultOfMethodCallIgnored
                        input.read(buffer)
                        input.close()

                        // Inject our script that 'steals' credentials from the page
                        view.evaluateJavascript(String(buffer), {})
                    }
                }
                dialog.show()
            }
            ExAuthFlow.Format.STANDARD -> TODO("Not yet supported")
            ExAuthFlow.Format.GOOGLE -> TODO("Not yet supported")
        }
        stateCallback(State.ENTER_CREDENTIALS)

    }
}