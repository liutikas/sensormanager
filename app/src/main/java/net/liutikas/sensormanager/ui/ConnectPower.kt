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

package net.liutikas.sensormanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            Icon(painter = painterResource(id = R.drawable.ic_power), contentDescription = null)
            Text(
                    text = "2. Wait for 1 minute",
                    style = MaterialTheme.typography.h4
            )
            Icon(painter = painterResource(id = R.drawable.ic_clock), contentDescription = null)
            Button(onClick = { navigation(ConfigureDeviceAppState()) }) {
                Text(text = "Continue")
            }
        }
    }
}

@Preview
@Composable
fun PreviewConnectPower() {
    PicturegramTheme {
        Surface {
            ConnectPower()
        }
    }
}