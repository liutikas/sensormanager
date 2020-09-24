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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import net.liutikas.sensormanager.state.AppState
import net.liutikas.sensormanager.state.ConfigureDeviceAppState
import net.liutikas.sensormanager.state.ConnectPowerAppState
import net.liutikas.sensormanager.state.ListDevicesAppState
import net.liutikas.sensormanager.ui.ConnectPower
import net.liutikas.sensormanager.ui.PicturegramTheme

class MainActivity : ComponentActivity() {
    private var appState: AppState by mutableStateOf(ListDevicesAppState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                when(appState) {
                    is ConfigureDeviceAppState -> {
                        configureDeviceScreen(this, appState as ConfigureDeviceAppState) { goToState(it) }
                    }
                    is ListDevicesAppState -> {
                        listDevicesScreen(this, appState as ListDevicesAppState) { goToState(it) }
                    }
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
}

@Composable
fun SubScreen(navigation: (AppState) -> Unit, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
                title = { Text("sensor.community")},
                navigationIcon = {
                    IconButton(onClick = { navigation(ListDevicesAppState())  }) {
                        Icon(asset = vectorResource(id = R.drawable.ic_back))
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
