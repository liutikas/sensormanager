/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liutikas.sensormanager

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.PatternMatcher
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi

@ChecksSdkIntAtLeast(api = 29)
fun isDeviceConfigurationAvailable() = Build.VERSION.SDK_INT >= 29

@RequiresApi(29)
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