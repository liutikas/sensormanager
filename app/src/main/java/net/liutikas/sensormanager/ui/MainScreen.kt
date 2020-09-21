package net.liutikas.sensormanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import net.liutikas.sensormanager.AppState
import net.liutikas.sensormanager.ConnectPowerAppState
import net.liutikas.sensormanager.ListDevicesAppState
import net.liutikas.sensormanager.R

@Composable
fun mainScreen(navigation: (AppState) -> Unit = {}) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("sensor.community") },
                navigationIcon = {
                    Image(asset = vectorResource(id = R.drawable.ic_sensors), modifier = Modifier.padding(16.dp))
                })
    }) {
        Column(Modifier.padding(32.dp)) {
            Button(onClick = { navigation(ConnectPowerAppState) }) {
                Text(text = "Configure new device")
            }
            Divider(color = Color.Transparent, thickness = 16.dp)
            Button(onClick = { navigation(ListDevicesAppState()) }) {
                Text(text = "List existing devices")
            }
        }
    }
}

@Preview
@Composable
fun previewMainScreen() {
    mainScreen()
}