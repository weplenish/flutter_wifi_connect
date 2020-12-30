# flutter_wifi_connect

A low dependency flutter plugin to allow simple connection to wifi devices with the provided ssid or ssid prefix. Built for Android 10+ (API 29) and iOS 11+.

This plugin is made with the latest / greatest direct connect to wifi options. It's intent is to make it easy to programmatically connect to devices that broadcast unique SSIDs. Items like IOT devices.

This should connect to an explicit SSID on iOS 11+ and Android 1+.
To connect to an SSID matching a prefix it should work on iOS 13+ and Android 29+.
On Android 1-29 it will scan nearby wifi networks and attempt to connect to the one matching the prefix.

## Getting Started

The following permissions are necessary for versions of android before 29.

- ACCESS_WIFI_STATE
- CHANGE_WIFI_STATE
- CHANGE_NETWORK_STATE
- ACCESS_FINE_LOCATION
