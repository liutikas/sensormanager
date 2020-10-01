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
import android.view.View
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import net.liutikas.sensormanager.state.AppState
import net.liutikas.sensormanager.state.ConfigureDeviceAppState
import net.liutikas.sensormanager.ui.WebViewContainer
import net.liutikas.sensormanager.ui.rememberWebViewWithLifecycle

@Composable
fun ConfigureDeviceScreen(
    context: Context,
    appState: ConfigureDeviceAppState,
    navigation: (AppState) -> Unit
) {
    SubScreen(navigation) {
        if (isDeviceConfigurationAvailable()) {
            appState.disconnectFromAccessPoint = remember { handleWifi(context) { appState.networkConnected = true } }
            Column(modifier = Modifier.fillMaxHeight()) {
                if (appState.networkConnected && appState.showConfigurationWebView) {
                    Text(modifier = Modifier.padding(16.dp), text = "Enter network name and password. Click save and restart")
                } else if (appState.networkConnected) {
                    Text(modifier = Modifier.padding(16.dp), text = "Setup successful. Device should be ready in a few minutes")
                    Icon(asset = vectorResource(id = R.drawable.ic_check))
                }
                val webview = rememberWebViewWithLifecycle {
                    appState.showConfigurationWebView = false
                }
                if (appState.networkConnected) {
                    webview.loadUrl("http://192.168.4.1/config")
                }
                Column(modifier = Modifier.weight(1f)) {
                    WebViewContainer(webview)
                }
                webview.visibility = if (appState.showConfigurationWebView) View.VISIBLE else View.GONE
            }
        } else {
            Text("New device configuration unavailable on this device.")
        }
    }
}