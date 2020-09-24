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

package net.liutikas.sensormanager.ui

import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import net.liutikas.sensormanager.R
import net.liutikas.sensormanager.SubScreen
import net.liutikas.sensormanager.state.AppState
import net.liutikas.sensormanager.state.ConfigureDeviceAppState

@Composable
fun ConnectPower(navigation: (AppState) -> Unit = {}) {
    SubScreen(navigation) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                    text = "1. Connect your sensor to power",
                    style = MaterialTheme.typography.h4
            )
            Icon(asset = vectorResource(id = R.drawable.ic_power))
            Text(
                    text = "2. Wait for 1 minute",
                    style = MaterialTheme.typography.h4
            )
            Icon(asset = vectorResource(id = R.drawable.ic_clock))
            Button(onClick = { navigation(ConfigureDeviceAppState()) }) {
                Text(text = "Continue")
            }
        }
    }
}

@Preview
@Composable
fun previewConnectPower() {
    PicturegramTheme {
        Surface {
            ConnectPower()
        }
    }
}