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
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUEST_CODE = 100
    private val OVERLAY_PERMISSION_REQUEST_CODE = 200
    private lateinit var webView: WebView
    private var currentUrl = "https://video.ssongg.cn"
    private var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initWebView()
        restoreState(savedInstanceState)
        loadUrl()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // 权限已授予
            } else {
                showToast("悬浮窗权限被拒绝")
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }
    }

    private fun setupWebChromeClient() {
        webView.webChromeClient = object : WebChromeClient() {
            private var customView: View? = null
            private var originalSystemUiVisibility: Int = 0

            // 处理全屏显示（仅保留必要逻辑）
            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                customView = view
                originalSystemUiVisibility = window.decorView.systemUiVisibility
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                callback.onCustomViewAttached(view)
            }

            // 处理全屏退出
            override fun onHideCustomView() {
                customView?.let {
                    it.visibility = View.GONE
                    customViewCallback?.onCustomViewHidden()
                    customView = null
                    window.decorView.systemUiVisibility = originalSystemUiVisibility
                }
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
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
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


    override fun onBackPressed() {
        if (webView.canGoBack()) { // 检查 WebView 是否有历史记录
            webView.goBack() // 返回上一级网页
        } else {
            super.onBackPressed() // 无历史记录时退出 App
        }
    }
    private fun showPermissionGuide() {
        AlertDialog.Builder(this)
            .setTitle("需要悬浮窗权限")
            .setMessage("请前往设置开启「显示在其他应用上层」权限")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun enterFullscreen() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        // 关键修复：禁用传感器方向检测
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }
    private fun exitFullscreen() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

}

private fun WebChromeClient.CustomViewCallback.onCustomViewAttached(view: View) {}
