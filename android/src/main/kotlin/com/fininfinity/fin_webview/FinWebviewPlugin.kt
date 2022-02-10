package com.fininfinity.fin_webview

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** FinWebviewPlugin */
class FinWebviewPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private  var channel : MethodChannel? = null
  private  var context: Context?  = null
  private  var activity: Activity? = null
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "fin_webview")
    if(channel!=null) {
        channel!!.setMethodCallHandler(this)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }else if(call.method.equals("showWebView")) {
     launchWebView(call,result)
    }
     else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
      if(channel!=null) channel!!.setMethodCallHandler(null)
  }

  private fun launchWebView(call: MethodCall, result:MethodChannel.Result){
        val webUrl = call.argument<String>("url")
        val title = call.argument<String>("title")
        val intent = Intent(activity, WebViewActivity::class.java)
        intent.putExtra("webUrl", webUrl);
        intent.putExtra("title", title);
        startActivity(activity!!, intent, null)
        result.success("ActivityStarted")
  }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        context = binding.activity.applicationContext
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {
        
    }
}
