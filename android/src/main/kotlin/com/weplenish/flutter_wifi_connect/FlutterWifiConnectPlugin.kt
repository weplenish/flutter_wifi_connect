package com.weplenish.flutter_wifi_connect

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PatternMatcher
import android.os.PatternMatcher.PATTERN_PREFIX
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.Error

/** FlutterWifiConnectPlugin */
class FlutterWifiConnectPlugin(private val activity: Activity) : FlutterPlugin, MethodCallHandler {
  // / The MethodChannel that will the communication between Flutter and native Android
  // /
  // / This local reference serves to register the plugin with the Flutter Engine and unregister it
  // / when the Flutter Engine is detached from the Activity
  private lateinit var channel: MethodChannel
  private var networkCallback: ConnectivityManager.NetworkCallback? = null

  private val connectivityManager: ConnectivityManager by lazy(LazyThreadSafetyMode.NONE) {
    activity.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
  }

  private val wifiManager: WifiManager by lazy(LazyThreadSafetyMode.NONE) {
    activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_wifi_connect")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "disconnect" -> {
      }
      "getSSID" -> {
      }
      "connect" -> {
        val ssid = call.argument<String>("ssid")
        ssid?.let {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                    .setSsid(it)
                    .build()
            connect(specifier, result)
          } else {
            val wifiConfig = createWifiConfig(it)
            connect(wifiConfig, result)
          }
        }
        return
      }
      "prefixConnect" -> {
        val ssid = call.argument<String>("ssid")
        ssid?.let {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val specifier = WifiNetworkSpecifier.Builder()
                    .setSsidPattern(PatternMatcher(it, PATTERN_PREFIX))
                    .build()
            connect(specifier, result)
            return
          }else{
            val correctSSID = getClosestSSIDMatchingPrefix(it)
            val wifiConfig = createWifiConfig(correctSSID)
            connect(wifiConfig, result)
            return
          }
        }
        return
      }
      "secureConnect" -> {
        val ssid = call.argument<String>("ssid")
        val password = call.argument<String>("password")
        val isWep = call.argument<Boolean>("isWep")

        if(ssid != null && password != null && isWep != null){
          if(isWep || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            val wifiConfig = isWep.let {
              if(it){
                createWEPConfig(ssid, password)
              }else{
                createWifiConfig(ssid, password)
              }
            }
            connect(wifiConfig, result)
            return
          }
          val specifier = WifiNetworkSpecifier.Builder()
                  .setSsid(ssid)
                  .setWpa2Passphrase(password)
                  .setWpa3Passphrase(password)
                  .build()
          connect(specifier, result)
        }
        return
      }
      "securePrefixConnect" -> {
        val ssid = call.argument<String>("ssid")
        val password = call.argument<String>("password")
        val isWep = call.argument<Boolean>("isWep")

        if(ssid != null && password != null && isWep != null){
          if(isWep || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            val wifiConfig = isWep.let {
              val correctSSID = getClosestSSIDMatchingPrefix(ssid)
              if(it){
                createWEPConfig(correctSSID, password)
              }else{
                createWifiConfig(correctSSID, password)
              }
            }
            connect(wifiConfig, result)
            return
          }
          val specifier = WifiNetworkSpecifier.Builder()
                  .setSsidPattern(PatternMatcher(ssid, PATTERN_PREFIX))
                  .setWpa2Passphrase(password)
                  .setWpa3Passphrase(password)
                  .build()
          connect(specifier, result)
        }
        return
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  fun getClosestSSIDMatchingPrefix(@NonNull ssidPrefix: String): String{
    //TODO: scan local networks pick closest with matching prefix
    throw Error("Incomplete")
  }

  @Suppress("DEPRECATION")
  fun createWifiConfig(@NonNull ssid: String): WifiConfiguration{
    return WifiConfiguration().apply {
      SSID = "\"" + ssid + "\""
      allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)

      allowedProtocols.set(WifiConfiguration.Protocol.RSN)
      allowedProtocols.set(WifiConfiguration.Protocol.WPA)

      allowedAuthAlgorithms.clear()

      allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
      allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)

      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
    }
  }

  @Suppress("DEPRECATION")
  fun createWifiConfig(@NonNull ssid: String, @NonNull password: String): WifiConfiguration{
    return createWifiConfig(ssid).apply {
      preSharedKey = "\"" + password + "\""
      status = WifiConfiguration.Status.ENABLED

      allowedKeyManagement.clear()
      allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
    }
  }

  @Suppress("DEPRECATION")
  fun createWEPConfig(@NonNull ssid: String, @NonNull password: String): WifiConfiguration{
    return createWifiConfig(ssid).apply {
      wepKeys[0] = "\"" + password + "\""
      wepTxKeyIndex = 0

      allowedGroupCiphers.clear()
      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
      allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

      allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
      allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
    }
  }

  @SuppressLint("MissingPermission")
  @Suppress("DEPRECATION")
  fun connect(@NonNull wifiConfiguration: WifiConfiguration, @NonNull result: Result){
    val network = wifiManager.addNetwork(wifiConfiguration)
    wifiManager.saveConfiguration()
    wifiManager.disconnect()
    wifiManager.enableNetwork(network, true)
    wifiManager.reconnect()
    //TODO: test for network connection, maybe some listener
    result.success(true)
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  fun connect(@NonNull specifier: WifiNetworkSpecifier, @NonNull result: Result){
    val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

    networkCallback?.let { connectivityManager.unregisterNetworkCallback(it) }
    networkCallback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        super.onAvailable(network)
        connectivityManager.bindProcessToNetwork(network)
        result.success(true)
      }

      override fun onUnavailable() {
        super.onUnavailable()
        result.success(false)
      }
    }
    networkCallback?.let {
      val handler = Handler(Looper.getMainLooper())
      connectivityManager.requestNetwork(request, it, handler)
    }
  }
}
