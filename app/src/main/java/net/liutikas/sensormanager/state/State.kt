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

package net.liutikas.sensormanager.state

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.liutikas.sensormanager.NsdServiceEvent
import net.liutikas.sensormanager.resolveService
import net.liutikas.sensormanager.serviceDiscovery
import net.liutikas.sensormanager.ui.SensorItemEntry

sealed class AppState {
    abstract fun tearDown()
}

class ConfigureDeviceAppState : AppState() {
    var networkConnected: Boolean by mutableStateOf(false)
    var showConfigurationWebView: Boolean by mutableStateOf(true)
    var disconnectFromAccessPoint: () -> Unit = {}

    override fun tearDown() {
        disconnectFromAccessPoint()
    }
}

object ConnectPowerAppState : AppState() {
    override fun tearDown() {
    }
}

class ListDevicesAppState : AppState() {
    private val _discoveredServices = mutableStateMapOf<String, NsdServiceInfo>()
    val discoveredServices: Map<String, NsdServiceInfo> get() = _discoveredServices
    private val _sensorItems = mutableStateMapOf<String, SensorItemEntry>()
    val sensorItems: Map<String, SensorItemEntry> get() = _sensorItems

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun runDiscovery(nsdManager: NsdManager) = coroutineScope {
        val resolveMutex = Mutex()
        nsdManager.serviceDiscovery("_http._tcp.")
            .onStart { println("starting service discovery") }
            .onEach { println("service discovery event: $it") }
            .onCompletion { println("ending service discovery; reason: $it") }
            .collect { (type, info) ->
                when (type) {
                    NsdServiceEvent.Type.Found -> {
                        // The concurrent accesses between this and the resolveService result
                        // writes below are ok because these collections are backed by snapshots.
                        // (Also we're called on a single-threaded dispatcher anyway.)
                        _discoveredServices[info.serviceName] = info
                        _sensorItems[info.serviceName] = SensorItemEntry(
                            info.serviceName,
                            info.host?.hostAddress,
                            true
                        )
                        launch {
                            // resolveService can't be used concurrently??
                            resolveMutex.withLock {
                                val resolved = nsdManager.resolveService(info)
                                _discoveredServices[info.serviceName] = resolved
                                _sensorItems[info.serviceName] = SensorItemEntry(
                                    resolved.serviceName,
                                    resolved.host?.hostAddress,
                                    false
                                )
                            }
                        }
                    }
                    NsdServiceEvent.Type.Lost -> {
                        _discoveredServices.remove(info.serviceName)
                        _sensorItems.remove(info.serviceName)
                    }
                }
            }
    }

    override fun tearDown() {
    }
}