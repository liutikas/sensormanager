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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.liutikas.sensormanager.R

@Composable
fun SensorItem(
        item: SensorItemEntry,
        open: () -> Unit = {},
) {
    Row {
        Image(
            painter = painterResource(id = R.drawable.ic_sensors),
            contentDescription = null,
            modifier = Modifier.padding(8.dp)
        )
        Column(Modifier.weight(1f)) {
            Text(item.name)
            if (item.ipAddress != null) {
                Text(
                        text = item.ipAddress,
                        style = MaterialTheme.typography.caption
                )
            } else {
                Text(
                        text = "IP address resolving...",
                        style = MaterialTheme.typography.caption
                )
            }
        }
        Column {
            if (item.isResolving) {
                CircularProgressIndicator()
            } else if (item.ipAddress != null) {
                Button(onClick = open) {
                    Text("Open")
                }
            }
        }
    }
}

data class SensorItemEntry(
        val name: String,
        val ipAddress: String? = null,
        val isResolving: Boolean
)

@Preview
@Composable
fun PreviewSensorItemDetected() {
    PicturegramTheme {
        Surface {
            SensorItem(SensorItemEntry("airRohr-123", "123.123.123.123", false))
        }
    }
}

@Preview
@Composable
fun PreviewSensorItemDetecting() {
    PicturegramTheme {
        Surface {
            SensorItem(SensorItemEntry("airRohr-123", null, false))
        }
    }
}
