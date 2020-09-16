package net.liutikas.picturegram

import android.content.Context
import android.content.Intent
import android.net.*
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build.VERSION.SDK_INT
import android.os.PatternMatcher
import androidx.core.content.ContextCompat.startActivity


fun handleWifi(context: Context) {
    if (SDK_INT >= 29) {
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
            override fun onUnavailable() {
                super.onUnavailable()
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)

            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                connectivityManager?.bindProcessToNetwork(network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
            }
        }
        connectivityManager?.requestNetwork(networkRequest, networkCallback)
    }
}