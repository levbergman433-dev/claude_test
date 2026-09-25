package com.maxrave.simpmusic.expect.ui

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

actual fun createWebViewCookieManager(): WebViewCookieManager =
    object : WebViewCookieManager {
        override fun getCookie(url: String): String {
            val cookie = CookieManager.getInstance()
            return if (cookie.hasCookies()) {
                cookie.getCookie(url)
            } else {
                ""
            }
        }

        override fun removeAllCookies() {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

@Composable
actual fun PlatformWebView(
    state: MutableState<WebViewState>,
    initUrl: String,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onPageFinished: (String) -> Unit,
) {
    var generation by remember { mutableIntStateOf(0) }
    Box {
        if (generation <= MAX_RENDERER_RESTARTS) {
            key(generation) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams =
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                            webViewClient =
                                object : WebViewClient() {
                                    override fun onPageFinished(
                                        view: WebView?,
                                        url: String?,
                                    ) {
                                        url?.let {
                                            onPageFinished(it)
                                        }
                                    }

                                    override fun onRenderProcessGone(
                                        view: WebView,
                                        detail: RenderProcessGoneDetail,
                                    ): Boolean {
                                        discardDeadWebView(view)
                                        generation++
                                        return true
                                    }
                                }
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true

                            loadUrl(initUrl)
                        }
                    },
                    onRelease = { releaseWebView(it) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        aboveContent()
    }
}

@Composable
actual fun DiscordWebView(
    state: MutableState<WebViewState>,
    aboveContent: @Composable (BoxScope.() -> Unit),
    onLoginDone: (String) -> Unit
) {
    val url = "https://discord.com/login"
    var generation by remember { mutableIntStateOf(0) }
    Box {
        if (generation <= MAX_RENDERER_RESTARTS) key(generation) {
        AndroidView(factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {

                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        webView: WebView,
                        url: String,
                    ): Boolean {
                        stopLoading()
                        if (url.endsWith("/app")) {
                            loadUrl(JS_SNIPPET)
                        }
                        return false
                    }

                    override fun onRenderProcessGone(
                        view: WebView,
                        detail: RenderProcessGoneDetail,
                    ): Boolean {
                        discardDeadWebView(view)
                        generation++
                        return true
                    }
                }
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                if (android.os.Build.MANUFACTURER.equals(MOTOROLA, ignoreCase = true)) {
                    settings.userAgentString = SAMSUNG_USER_AGENT
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onJsAlert(
                        view: WebView,
                        url: String,
                        message: String,
                        result: JsResult,
                    ): Boolean {
                        onLoginDone(message)
                        return true
                    }
                }
                loadUrl(url)
            }
        }, onRelease = { releaseWebView(it) })
        }
        aboveContent()
    }
}

/**
 * How many times a login page is rebuilt after its renderer process dies before giving up. The
 * renderer can die on its own (out of memory, a crash inside Chromium); without an
 * onRenderProcessGone handler Android then kills the WHOLE app and blames WebView ("Uninstall
 * WebView updates?"). Handling it keeps the app alive, and a fresh WebView reloads the page. The
 * cap stops a page that kills its renderer every time from looping forever.
 */
private const val MAX_RENDERER_RESTARTS = 3

/** Marks a WebView whose renderer died as already destroyed, so onRelease does not touch it. */
private const val DEAD_WEBVIEW_TAG = "renderer-gone"

/**
 * A WebView whose renderer is gone must never be drawn or used again: take it out of the view
 * hierarchy and destroy it right away. The caller then bumps the generation to build a new one.
 */
private fun discardDeadWebView(view: WebView) {
    view.tag = DEAD_WEBVIEW_TAG
    (view.parent as? ViewGroup)?.removeView(view)
    view.destroy()
}

/** Frees a WebView when its composable leaves, unless it was already discarded as dead. */
private fun releaseWebView(view: WebView) {
    if (view.tag == DEAD_WEBVIEW_TAG) return
    view.stopLoading()
    view.destroy()
}

const val JS_SNIPPET =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"
private const val MOTOROLA = "motorola"
private const val SAMSUNG_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"