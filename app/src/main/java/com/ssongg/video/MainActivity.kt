package com.ssongg.video

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var originalOrientation: Int = 0
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
            mediaPlaybackRequiresUserGesture = true // 允许自动播放
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // 处理URL加载
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                view.loadUrl(request.url.toString())
                return true
            }
        }
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            // 处理全屏显示
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                super.onShowCustomView(view, callback)
                if (customView != null) {
                    callback.onCustomViewHidden()
                    return
                }

                originalOrientation = requestedOrientation
                customView = view
                customViewCallback = callback

                // 设置全屏和横屏
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

                // 将自定义视图添加到Activity
                val decor = window.decorView as FrameLayout
                decor.addView(customView, FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ))
            }

            // 处理全屏退出
            override fun onHideCustomView() {
                super.onHideCustomView()
                if (customView == null) return

                // 恢复原始设置
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                requestedOrientation = originalOrientation

                // 移除自定义视图
                val decor = window.decorView as FrameLayout
                decor.removeView(customView)
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }
        }
    }

    // 返回键处理：优先返回WebView历史，否则退出Activity
    override fun onBackPressed() {
        if (customView != null) {
            // 如果处于全屏状态，先退出全屏
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            // 如果WebView有历史记录，返回上一页
            webView.goBack()
        } else {
            // 否则退出Activity
            super.onBackPressed()
        }
    }

    // 确保Activity销毁时释放资源
    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}

