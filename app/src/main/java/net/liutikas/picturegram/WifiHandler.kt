package net.liutikas.picturegram

import android.content.Context
import android.content.Intent
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build.VERSION.SDK_INT
import android.os.PatternMatcher
import androidx.core.content.ContextCompat.startActivity


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