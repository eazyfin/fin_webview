import 'dart:async';

import 'package:flutter/services.dart';

class FinWebview {
  static const MethodChannel _channel = MethodChannel('fin_webview');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static showWebView(title, url) async {
    await _channel.invokeMethod('showWebView', {'url': url});
  }
}
