package net.liutikas.sensormanager

import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import net.liutikas.sensormanager.ui.*

class MainActivity : AppCompatActivity() {
    enum class AppState {
        MAIN,
        CONNECT_POWER,
        CONFIGURE_DEVICE,
        LIST_DEVICES
    }

    private var appState: AppState by mutableStateOf(AppState.MAIN)
    var startLoading: Boolean by mutableStateOf(false)
    var showConfigurationWebView: Boolean by mutableStateOf(true)

    val discoveredServices = mutableMapOf<String, NsdServiceInfo>()
    var sensorItems: List<SensorItemEntry> by mutableStateOf(emptyList())
    var disconnect: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                when(appState) {
                    AppState.MAIN -> mainScreen { goToState(it) }
                    AppState.CONFIGURE_DEVICE -> configureDeviceScreen { goToState(it) }
                    AppState.LIST_DEVICES -> listDevicesScreen { goToState(it) }
                    AppState.CONNECT_POWER -> ConnectPower { goToState(it) }
                }
            }
        }
    }

    private fun goToState(newState: AppState) {
        if (newState == AppState.MAIN) {
            startLoading = false
            showConfigurationWebView = true
            discoveredServices.clear()
            sensorItems = emptyList()
            disconnect()
        }
        appState = newState
    }

    override fun onBackPressed() {
        if (appState == AppState.MAIN) {
            super.onBackPressed()
        } else {
            goToState(AppState.MAIN)
        }
    }

    private fun updateSensorItems() {
        sensorItems = discoveredServices.map {
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

    @Composable
    fun configureDeviceScreen(navigation: (AppState) -> Unit) {
        SubScreen(navigation) {
            disconnect = remember { handleWifi(this) { startLoading = true } }
            Column(modifier = Modifier.fillMaxHeight()) {
                if (showConfigurationWebView) {
                    Text(modifier = Modifier.padding(16.dp), text = "Enter network name and password. Click save and restart")
                } else {
                    Text(modifier = Modifier.padding(16.dp), text = "Setup successful. Device should be ready in a few minutes")
                    Image(asset = vectorResource(id = R.drawable.ic_check))
                }
                val webview = rememberWebViewWithLifecycle {
                    showConfigurationWebView = false
                }
                if (startLoading) {
                    webview.loadUrl("http://192.168.4.1/config")
                }
                Column(modifier = Modifier.weight(1f)) {
                    WebViewContainer(webview)
                }
                webview.visibility = if (showConfigurationWebView) View.VISIBLE else View.GONE
            }
        }
    }

    @Composable
    fun listDevicesScreen(navigation: (AppState) -> Unit) {
        SubScreen(navigation) {
            Column(Modifier.padding(32.dp)) {
                remember { setupLocalDiscovery(this@MainActivity) { service ->
                    discoveredServices[service.serviceName] = service
                    updateSensorItems()
                }
                }
                if (sensorItems.isEmpty()) {
                    Text("Searching for sensor.community devices on the local network")
                } else {
                    for (item in sensorItems) {
                        SensorItem(
                                item,
                                resolve = {
                                    sensorItems = discoveredServices.map {
                                        SensorItemEntry(it.value.serviceName, if(it.value.host != null) it.value.host.hostAddress else null, it.value.serviceName == item.name)
                                    }
                                    resolveService(this@MainActivity, discoveredServices[item.name]) { service ->
                                        discoveredServices[service.serviceName] = service
                                        updateSensorItems()
                                    }
                                },
                                open = {
                                    openService(this@MainActivity, discoveredServices[item.name]!!)
                                }
                        )
                        Divider(color = Color.Transparent, thickness = 16.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun SubScreen(navigation: (MainActivity.AppState) -> Unit, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
                title = { Text("sensor.community")},
                navigationIcon = {
                    IconButton(onClick = { navigation(MainActivity.AppState.MAIN)  }) {
                        Image(asset = vectorResource(id = R.drawable.ic_back))
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
