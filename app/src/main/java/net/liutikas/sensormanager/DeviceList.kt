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
import android.net.nsd.NsdManager
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.launchInComposition
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import net.liutikas.sensormanager.state.AppState
import net.liutikas.sensormanager.state.ConnectPowerAppState
import net.liutikas.sensormanager.state.ListDevicesAppState
import net.liutikas.sensormanager.ui.SensorItem

@Composable
fun ListDevicesScreen(
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
            if (isDeviceConfigurationAvailable()) {
                Button(onClick = { navigation(ConnectPowerAppState) }) {
                    Text(text = "Configure new device")
                }
                Divider(color = Color.Transparent, thickness = 16.dp)
            }

            launchInComposition {
                val nsdManager = context.getSystemService<NsdManager>() ?: error("NsdManager not available")
                appState.runDiscovery(nsdManager)
            }

            Text("Local sensor.community devices", style = MaterialTheme.typography.h6)
            Divider(color = Color.Transparent, thickness = 16.dp)
            if (appState.sensorItems.isEmpty()) {
                Text("Searching for devices")
            } else {
                for ((_, item) in appState.sensorItems) {
                    SensorItem(
                        item,
                        open = {
                            openService(context,
                                appState.discoveredServices[item.name] ?: error("Tried to open a sensor URL before IP was resolved")
                            )
                        }
                    )
                    Divider(color = Color.Transparent, thickness = 16.dp)
                }
            }
        }
    }
}
