/*
 * Copyright 2020 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.liutikas.sensormanager

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import net.liutikas.sensormanager.ui.*

sealed class AppState {
    abstract fun tearDown()
}

class ConfigureDeviceAppState : AppState() {
    var startLoading: Boolean by mutableStateOf(false)
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
    val discoveredServices = mutableMapOf<String, NsdServiceInfo>()
    var sensorItems: List<SensorItemEntry> by mutableStateOf(emptyList())
    var stopServiceDiscovery: () -> Unit = {}

    override fun tearDown() {
        stopServiceDiscovery()
    }
}

class MainActivity : AppCompatActivity() {
    private var appState: AppState by mutableStateOf(ListDevicesAppState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                when(appState) {
                    is ConfigureDeviceAppState -> configureDeviceScreen { goToState(it) }
                    is ListDevicesAppState -> listDevicesScreen { goToState(it) }
                    ConnectPowerAppState -> ConnectPower { goToState(it) }
                }
            }
        }
    }

    private fun goToState(newState: AppState) {
        appState.tearDown()
        appState = newState
    }

    override fun onBackPressed() {
        if (appState is ListDevicesAppState) {
            appState.tearDown()
            super.onBackPressed()
        } else {
            goToState(ListDevicesAppState())
        }
    }

    private fun updateSensorItems(listDevices: ListDevicesAppState) {
        listDevices.sensorItems = listDevices.discoveredServices.map {
            SensorItemEntry(
                    it.value.serviceName,
                    if (it.value.host != null) {
                        it.value.host.hostAddress
                    } else {
                        null
                    },
                    false
            )
        }
    }

    @Composable
    fun configureDeviceScreen(navigation: (AppState) -> Unit) {
        SubScreen(navigation) {
            val configureDevice = appState as ConfigureDeviceAppState
            configureDevice.disconnectFromAccessPoint = remember { handleWifi(this) { configureDevice.startLoading = true } }
            Column(modifier = Modifier.fillMaxHeight()) {
                if (configureDevice.showConfigurationWebView) {
                    Text(modifier = Modifier.padding(16.dp), text = "Enter network name and password. Click save and restart")
                } else {
                    Text(modifier = Modifier.padding(16.dp), text = "Setup successful. Device should be ready in a few minutes")
                    Image(asset = vectorResource(id = R.drawable.ic_check))
                }
                val webview = rememberWebViewWithLifecycle {
                    configureDevice.showConfigurationWebView = false
                }
                if (configureDevice.startLoading) {
                    webview.loadUrl("http://192.168.4.1/config")
                }
                Column(modifier = Modifier.weight(1f)) {
                    WebViewContainer(webview)
                }
                webview.visibility = if (configureDevice.showConfigurationWebView) View.VISIBLE else View.GONE
            }
        }
    }

    @Composable
    fun listDevicesScreen(navigation: (AppState) -> Unit) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("sensor.community") },
                    navigationIcon = {
                        Image(asset = vectorResource(id = R.drawable.ic_sensors), modifier = Modifier.padding(16.dp))
                    })
        }) {
            val listDevices = appState as ListDevicesAppState
            Column(Modifier.padding(32.dp)) {
                Button(onClick = { navigation(ConnectPowerAppState) }) {
                    Text(text = "Configure new device")
                }
                Divider(color = Color.Transparent, thickness = 16.dp)

                listDevices.stopServiceDiscovery = remember {
                    setupLocalDiscovery(this@MainActivity) { service ->
                        listDevices.discoveredServices[service.serviceName] = service
                        updateSensorItems(listDevices)
                    }
                }
                Text("Local sensor.community devices", style = MaterialTheme.typography.h6)
                Divider(color = Color.Transparent, thickness = 16.dp)
                if (listDevices.sensorItems.isEmpty()) {
                    Text("Searching for devices")
                } else {
                    for (item in listDevices.sensorItems) {
                        SensorItem(
                                item,
                                resolve = {
                                    listDevices.sensorItems = listDevices.discoveredServices.map {
                                        SensorItemEntry(it.value.serviceName, if(it.value.host != null) it.value.host.hostAddress else null, it.value.serviceName == item.name)
                                    }
                                    resolveService(this@MainActivity, listDevices.discoveredServices[item.name]) { service ->
                                        listDevices.discoveredServices[service.serviceName] = service
                                        updateSensorItems(listDevices)
                                    }
                                },
                                open = {
                                    openService(this@MainActivity, listDevices.discoveredServices[item.name]!!)
                                }
                        )
                        Divider(color = Color.Transparent, thickness = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreen(navigation: (AppState) -> Unit, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
                title = { Text("sensor.community")},
                navigationIcon = {
                    IconButton(onClick = { navigation(ListDevicesAppState())  }) {
                        Image(asset = vectorResource(id = R.drawable.ic_back))
                    }
                },
        )
    }) {
        content()
    }
}

@Composable
fun MyApp(content: @Composable () -> Unit) {
    PicturegramTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}
