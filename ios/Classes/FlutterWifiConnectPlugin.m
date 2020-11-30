#import "FlutterWifiConnectPlugin.h"
#if __has_include(<flutter_wifi_connect/flutter_wifi_connect-Swift.h>)
#import <flutter_wifi_connect/flutter_wifi_connect-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_wifi_connect-Swift.h"
#endif

@implementation FlutterWifiConnectPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterWifiConnectPlugin registerWithRegistrar:registrar];
}
@end
