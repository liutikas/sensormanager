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
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import net.liutikas.sensormanager.state.AppState
import net.liutikas.sensormanager.state.ConnectPowerAppState
import net.liutikas.sensormanager.state.ListDevicesAppState
import net.liutikas.sensormanager.ui.SensorItem
import net.liutikas.sensormanager.ui.SensorItemEntry

@Composable
fun listDevicesScreen(
    context: Context,
    appState: ListDevicesAppState,
    navigation: (AppState) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("sensor.community") },
            navigationIcon = {
                Icon(asset = vectorResource(id = R.drawable.ic_sensors), modifier = Modifier.padding(16.dp))
            })
    }) {
        ScrollableColumn(Modifier.padding(32.dp)) {
            Button(onClick = { navigation(ConnectPowerAppState) }) {
                Text(text = "Configure new device")
            }
            Divider(color = Color.Transparent, thickness = 16.dp)

            appState.stopServiceDiscovery = remember {
                setupLocalDiscovery(context) { service ->
                    appState.discoveredServices[service.serviceName] = service
                    updateSensorItems(appState)
                }
            }
            Text("Local sensor.community devices", style = MaterialTheme.typography.h6)
            Divider(color = Color.Transparent, thickness = 16.dp)
            if (appState.sensorItems.isEmpty()) {
                Text("Searching for devices")
            } else {
                for (item in appState.sensorItems) {
                    SensorItem(
                        item,
                        resolve = {
                            appState.sensorItems = appState.discoveredServices.map {
                                SensorItemEntry(it.value.serviceName, if(it.value.host != null) it.value.host.hostAddress else null, it.value.serviceName == item.name)
                            }
                            resolveService(context, appState.discoveredServices[item.name]) { service ->
                                appState.discoveredServices[service.serviceName] = service
                                updateSensorItems(appState)
                            }
                        },
                        open = {
                            openService(context, appState.discoveredServices[item.name]!!)
                        }
                    )
                    Divider(color = Color.Transparent, thickness = 16.dp)
                }
            }
        }
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