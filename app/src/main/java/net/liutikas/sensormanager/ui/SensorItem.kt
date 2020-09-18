package net.liutikas.sensormanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import net.liutikas.sensormanager.R

@Composable
fun SensorItem(
        item: SensorItemEntry,
        resolve: () -> Unit = {},
        open: () -> Unit = {},
) {
    Row {
        Image(asset = vectorResource(id = R.drawable.ic_sensors), modifier = Modifier.padding(8.dp))
        Column(Modifier.weight(1f)) {
            Text(item.name)
            if (item.ipAddress != null) {
                Text(
                        text = item.ipAddress,
                        style = MaterialTheme.typography.caption
                )
            } else {
                Text(
                        text = "IP address pending",
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
            } else {
                Button(onClick = resolve) {
                    Text("Resolve IP")
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
fun previewSensorItemDetected() {
    PicturegramTheme {
        Surface {
            SensorItem(SensorItemEntry("airRohr-123", "123.123.123.123", false))
        }
    }
}

@Preview
@Composable
fun previewSensorItemDetecting() {
    PicturegramTheme {
        Surface {
            SensorItem(SensorItemEntry("airRohr-123", null, false))
        }
    }
}
