package com.ssongg.video

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var webView: WebView
    private var currentUrl = "https://video.ssongg.cn"
    private var isFullscreen = false
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        initWebView()
        restoreState(savedInstanceState)
        loadUrl()
    }

    private fun checkPermissions() {
        checkStoragePermission()
    }

    private fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun initWebView() {
        webView = findViewById(R.id.webview)
        setupWebViewSettings()
        setupWebChromeClient()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebViewSettings() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            cacheMode = android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): Boolean {
                val url = request.url.toString()
                // 排除需要跳转浏览器的情况（如电话、邮件）
                if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    return false
                }
                view.loadUrl(url) // 强制在 WebView 内加载
                return true
            }
        }

        // 启用硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                super.onShowCustomView(view, callback)
                if (isFullscreen) return

                customView = view
                customViewCallback = callback
                setFullscreen(true)
                setContentView(customView)
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                if (!isFullscreen) return

                setFullscreen(false)
                customView?.visibility = View.GONE
                customView = null
                customViewCallback?.onCustomViewHidden()
                setContentView(webView)
            }
        }
    }

    private fun setFullscreen(isFullscreen: Boolean) {
        this.isFullscreen = isFullscreen
        WindowInsetsControllerCompat(window, window.decorView).apply {
            if (isFullscreen) {
                hide(WindowInsetsCompat.Type.systemBars())
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                show(WindowInsetsCompat.Type.systemBars())
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun loadUrl() {
        webView.loadUrl(currentUrl)
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            currentUrl = it.getString("SAVED_URL") ?: currentUrl
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SAVED_URL", webView.url.toString())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                webView.reload()
            } else {
                showToast("存储权限被拒绝")
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (isFullscreen) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Suppress("DEPRECATION")
private fun MainActivity.exitFullscreen() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

@Suppress("DEPRECATION")
private fun MainActivity.enterFullscreen() {
    window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )

    // 关键修复：禁用传感器方向检测
    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

private fun MainActivity.showPermissionGuide() {
    AlertDialog.Builder(this)
        .setTitle("需要悬浮窗权限")
        .setMessage("请前往设置开启「显示在其他应用上层」权限")
        .setPositiveButton("去设置") { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = "package:$packageName".toUri()
            startActivity(intent)
        }
        .setNegativeButton("取消", null)
        .show()
}