package net.liutikas.sensormanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.PatternMatcher

fun handleWifi(context: Context, networkReadyListener: () -> Unit): () -> Unit {
    val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsidPattern(PatternMatcher("airRohr-", PatternMatcher.PATTERN_PREFIX))
            .build()

    val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            connectivityManager?.bindProcessToNetwork(network)
            networkReadyListener()
        }
    }
    connectivityManager?.requestNetwork(networkRequest, networkCallback)

    return { connectivityManager?.unregisterNetworkCallback(networkCallback) }
}