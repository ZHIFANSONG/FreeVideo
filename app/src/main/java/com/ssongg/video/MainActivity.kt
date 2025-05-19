package com.ssongg.video

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = 0
    private val url = "https://video.ssongg.cn" // 你的网站URL

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化WebView
        webView = findViewById(R.id.webview)
        setupWebView()
        setupWebChromeClient()
        webView.loadUrl(url)
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false // 允许自动播放
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            allowFileAccess = true
            builtInZoomControls = true
            displayZoomControls = false
        }

        // 处理URL加载
        webView.webViewClient = object : WebViewClient() {
            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }

            // 处理SSL错误（可选）
            override fun onReceivedSslError(view: WebView, handler: android.webkit.SslErrorHandler, error: android.net.http.SslError) {
                handler.proceed() // 忽略SSL证书错误，谨慎使用
            }
        }
    }

    // MainActivity.kt
    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            private var fullscreenView: View? = null
            private var fullscreenCallback: CustomViewCallback? = null

            // 处理全屏请求
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (fullscreenView != null) {
                    callback.onCustomViewHidden()
                    return
                }

                fullscreenView = view
                fullscreenCallback = callback

                // 隐藏系统栏，设置横屏
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                // 将全屏视图添加到窗口
                val decorView = window.decorView as FrameLayout
                decorView.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }

            // 退出全屏
            override fun onHideCustomView() {
                fullscreenView?.let {
                    val decorView = window.decorView as FrameLayout
                    decorView.removeView(it)
                    fullscreenView = null
                    fullscreenCallback?.onCustomViewHidden()
                    fullscreenCallback = null

                    // 恢复竖屏和系统栏
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }

    // 返回键处理：优先返回WebView历史，否则退出Activity
    // MainActivity.kt
    override fun onBackPressed() {
        val fullscreenView = null
        if (fullscreenView != null) { // 先处理全屏退出
            webView.webChromeClient?.onHideCustomView()
            return
        }

        if (webView.canGoBack()) { // 返回 WebView 历史
            webView.goBack()
        } else { // 退出应用
            super.onBackPressed()
        }
    }

    // 确保Activity销毁时释放资源
    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }

    // 暂停/恢复WebView
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }
}