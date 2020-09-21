package net.liutikas.sensormanager.ui

import androidx.compose.foundation.Image
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
import net.liutikas.sensormanager.AppState
import net.liutikas.sensormanager.ConfigureDeviceAppState
import net.liutikas.sensormanager.R
import net.liutikas.sensormanager.SubScreen

@Composable
fun ConnectPower(navigation: (AppState) -> Unit = {}) {
    SubScreen(navigation) {
        Column(Modifier.padding(16.dp), horizontalGravity = Alignment.CenterHorizontally) {
            Text(
                    text = "1. Connect your sensor to power",
                    style = MaterialTheme.typography.h4
            )
            Image(asset = vectorResource(id = R.drawable.ic_power))
            Text(
                    text = "2. Wait for 1 minute",
                    style = MaterialTheme.typography.h4
            )
            Image(asset = vectorResource(id = R.drawable.ic_clock))
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