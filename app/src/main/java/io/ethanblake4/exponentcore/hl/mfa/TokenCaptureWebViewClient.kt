package io.ethanblake4.exponentcore.hl.mfa

import android.os.Handler
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * Handles capture of token for 2FA
 * Attach to a WebView via [WebView.setWebViewClient]
 * Use [MFAWebViewUtil] for a higher level interface
 */
class TokenCaptureWebViewClient(val tokenCallback: (String) -> Unit) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String) {
        val cookies = CookieManager.getInstance().getCookie(url)

        if (cookies.contains("oauth_code=") || cookies.contains("oauth_token=")) {
            Handler().postDelayed({
                cookies.split(";").map {
                    Pair(it.split("=")[0], it.split("=")[1])
                }.firstOrNull { it.first == "oauth_token" }?.second?.trim()?.let(tokenCallback)
            }, 400)
        }
    }
}