#import "FinWebviewPlugin.h"
#if __has_include(<fin_webview/fin_webview-Swift.h>)
#import <fin_webview/fin_webview-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "fin_webview-Swift.h"
#endif

@implementation FinWebviewPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFinWebviewPlugin registerWithRegistrar:registrar];
}
@end
