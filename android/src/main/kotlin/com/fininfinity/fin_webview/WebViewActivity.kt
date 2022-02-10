package com.fininfinity.fin_webview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import io.flutter.plugin.common.MethodChannel

class WebViewActivity : FlutterActivity() {
    var webView: WebView? = null
    private var mChoseFileName: String? = null
    private var mUriCallback: ValueCallback<Uri?>? = null
    private var mUriListCallback: ValueCallback<Array<Uri>>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (Build.VERSION.SDK_INT >= 21) {
            var results: Array<Uri>? = null
            if (resultCode == RESULT_OK) {
                if (requestCode == REQUEST_CODE) {
                    if (null == mUriListCallback) {
                        return
                    }
                    if (intent == null) {
                        if (mChoseFileName != null) {
                            results = arrayOf(Uri.parse(mChoseFileName))
                        }
                    } else {
                        val dataString = intent.dataString
                        if (dataString != null) {
                            results = arrayOf(Uri.parse(dataString))
                        }
                    }
                }
            }
            mUriListCallback!!.onReceiveValue(results)
            mUriListCallback = null
        } else {
            if (requestCode == REQUEST_CODE) {
                if (null == mUriCallback) return
                val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
                mUriCallback!!.onReceiveValue(result)
                mUriCallback = null
            }
        }
    }
    @SuppressLint("SetJavaScriptEnabled", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val doneButton = Button(this)
        doneButton.setText("Done")
        val layoutParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
            )
        } else {
            TODO("VERSION.SDK_INT < LOLLIPOP")
        }
        layoutParams.gravity = Gravity.END
        layoutParams.marginEnd = 10
        layoutParams.height = 70
        doneButton.setLayoutParams(layoutParams)
        doneButton.setBackgroundResource(R.drawable.btn_shape);


        doneButton.setOnClickListener(View.OnClickListener {
            finish()
        })
        toolbar.setNavigationOnClickListener {
            // back button pressed
            finish()
        }
        toolbar.addView(doneButton)
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) !== PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) !== PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this@WebViewActivity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                1
            )
        }
        webView = findViewById<WebView>(R.id.ifView)
        val webSettings = webView!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = 0
            webView!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
        val webUrl: String? = intent.getStringExtra("webUrl");
        val title: String? = intent.getStringExtra("title");
        toolbar.setTitle(title)
        webView!!.webViewClient = Callback()
        if(webUrl==null){
            Toast.makeText(this@WebViewActivity, "Invalid Url", Toast.LENGTH_LONG).show()
        }else{
            webView!!.loadUrl(webUrl)
        }
        webView!!.webChromeClient = object : WebChromeClient() {
            //For Android 5.0+
            override fun onShowFileChooser(
                webView: WebView, filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (mUriListCallback != null) {
                    mUriListCallback!!.onReceiveValue(null)
                }
                mUriListCallback = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@WebViewActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mChoseFileName)
                    } catch (ex: IOException) {
                        Log.e(TAG, "Image file creation failed", ex)
                    }
                    if (photoFile != null) {
                        mChoseFileName = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?>
                intentArray = takePictureIntent.let { arrayOf(it) } ?: arrayOfNulls(0)
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, REQUEST_CODE)
                return true
            }
        }
    }

    inner class Callback : WebViewClient() {
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            MethodChannel(
                flutterEngine!!.dartExecutor.binaryMessenger,
                "FinMappFlutterMethodChannel"
            ).invokeMethod("url_callback", url)

            super.doUpdateVisitedHistory(view, url, isReload)
        }

        override fun onReceivedError(
            view: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            Toast.makeText(applicationContext, "Failed loading app!", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(
                Date()
            )
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    override fun onKeyDown(keyCode: Int, @NonNull event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webView!!.canGoBack()) {
                        webView!!.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private val TAG = WebViewActivity::class.java.simpleName
        private const val REQUEST_CODE = 1
    }
}