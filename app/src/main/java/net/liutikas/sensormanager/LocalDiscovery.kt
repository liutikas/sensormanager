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
import android.content.Intent
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun openService(context: Context, serviceInfo: NsdServiceInfo) {
    val url = "http://${serviceInfo.host.hostAddress}"
    val i = Intent(Intent.ACTION_VIEW)
    i.data = Uri.parse(url)
    context.startActivity(i)
}

class NetworkServiceDiscoveryFailedException(
    errorCode: Int
) : Exception("network service discovery failed; error $errorCode")

data class NsdServiceEvent(
    val type: Type,
    val serviceInfo: NsdServiceInfo,
) {
    enum class Type { Lost, Found }
}

@Suppress("ThrowableNotThrown")
@OptIn(ExperimentalCoroutinesApi::class)
fun NsdManager.serviceDiscovery(serviceType: String) = channelFlow<NsdServiceEvent> {
    val listener = object : NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            close(NetworkServiceDiscoveryFailedException(errorCode))
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            close(NetworkServiceDiscoveryFailedException(errorCode))
        }

        override fun onDiscoveryStarted(serviceType: String?) {
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            close()
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            offer(NsdServiceEvent(NsdServiceEvent.Type.Found, serviceInfo))
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            offer(NsdServiceEvent(NsdServiceEvent.Type.Lost, serviceInfo))
        }
    }

    discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, listener)
    awaitClose { stopServiceDiscovery(listener) }
}

suspend fun NsdManager.resolveService(
    serviceInfo: NsdServiceInfo
): NsdServiceInfo = suspendCancellableCoroutine { co ->
    val listener = object : NsdManager.ResolveListener {
        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            co.resume(serviceInfo)
        }

        @Suppress("ThrowableNotThrown")
        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            co.resumeWithException(NetworkServiceDiscoveryFailedException(errorCode))
        }
    }
    resolveService(serviceInfo, listener)
}