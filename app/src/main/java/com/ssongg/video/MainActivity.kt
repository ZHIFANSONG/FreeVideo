package com.ssongg.video

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import android.os.Build // 新增导入
import android.view.ViewGroup // 新增导入
import android.webkit.SslError

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var isFullscreen = false

    private val url = "https://video.ssongg.cn" // 你的网站URL

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
            mediaPlaybackRequiresUserGesture = false // 允许自动播放视频（需 Android 11+ 适配）
            setSupportMultipleWindows(true) // 支持多窗口（全屏所需）
            javaScriptCanOpenWindowsAutomatically = true // 允许JavaScript打开全屏窗口
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW // 允许混合内容（HTTP/HTTPS）
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
            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) { // 修正：导入SslError
                handler.proceed() // 忽略SSL错误，谨慎使用
            }
        }
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            // 处理视频全屏请求
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }

                customView = view
                customViewCallback = callback
                isFullscreen = true

                // 启用沉浸式全屏模式（隐藏系统栏）
                enableImmersiveMode()

                // 强制设置为横屏
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                // 将全屏视图添加到窗口
                val decorView = window.decorView as FrameLayout
                decorView.addView(view, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, // 修正：导入ViewGroup
                    ViewGroup.LayoutParams.MATCH_PARENT  // 修正：导入ViewGroup
                ))
            }

            // 退出全屏
            override fun onHideCustomView() {
                super.onHideCustomView()
                customView = null
                customViewCallback = null
                isFullscreen = false

                // 恢复系统栏显示
                disableImmersiveMode()

                // 恢复竖屏
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                // 移除全屏视图
                val decorView = window.decorView as FrameLayout
                decorView.removeView(customView)
            }

            // 可选：处理JavaScript对话框
            override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                result.confirm()
                return true
            }
        }
    }

    // 沉浸式全屏模式（适配不同API版本）
    @SuppressLint("ObsoleteSdkInt")
    private fun enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 修正：导入Build
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    // 退出沉浸式模式
    @SuppressLint("ObsoleteSdkInt")
    private fun disableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // 修正：导入Build
            window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    // 返回键处理：优先退出全屏，其次返回WebView历史，最后退出应用
    override fun onBackPressed() {
        if (isFullscreen) {
            webView.webChromeClient?.onHideCustomView()
            return
        }
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    // 释放资源
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