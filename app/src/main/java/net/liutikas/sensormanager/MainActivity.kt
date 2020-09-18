package net.liutikas.sensormanager

import android.annotation.SuppressLint
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.ui.tooling.preview.Preview
import net.liutikas.sensormanager.ui.ConnectPower
import net.liutikas.sensormanager.ui.PicturegramTheme
import net.liutikas.sensormanager.ui.SensorItem
import net.liutikas.sensormanager.ui.SensorItemEntry

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
                    AppState.MAIN -> {
                        mainScreen { newState ->  appState = newState }
                    }
                    AppState.CONFIGURE_DEVICE -> {
                        configureDeviceScreen()
                    }
                    AppState.LIST_DEVICES -> {
                        listDevicesScreen()
                    }
                    AppState.CONNECT_POWER -> {
                        ConnectPower { newState ->
                            appState = newState
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (appState == AppState.MAIN) {
            super.onBackPressed()
        } else {
            backToMainScreen()
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

    private fun backToMainScreen() {
        appState = AppState.MAIN
        startLoading = false
        showConfigurationWebView = true
        discoveredServices.clear()
        sensorItems = emptyList()
        disconnect()
    }

    @Composable
    fun configureDeviceScreen() {
        SubScreen(mainScreen = { backToMainScreen() }) {
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
                webview.visibility = if(showConfigurationWebView) View.VISIBLE else View.GONE
            }
        }
    }

    @Composable
    fun listDevicesScreen() {
        SubScreen({ backToMainScreen() }) {
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
fun SubScreen(mainScreen: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
                title = { Text("sensor.community")},
                navigationIcon = {
                    IconButton(onClick = { mainScreen()  }) {
                        Image(asset = vectorResource(id = R.drawable.ic_back))
                    }
                },
        )
    }) {
        content()
    }
}

@Composable
fun mainScreen(navigation: (MainActivity.AppState) -> Unit = {}) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("sensor.community") },
        navigationIcon = {
            Image(asset = vectorResource(id = R.drawable.ic_sensors), modifier = Modifier.padding(16.dp))
        })
    }) {
        Column(Modifier.padding(32.dp)) {
            Button(onClick = { navigation(MainActivity.AppState.CONNECT_POWER) }) {
                Text(text = "Configure new device")
            }
            Divider(color = Color.Transparent, thickness = 16.dp)
            Button(onClick = { navigation(MainActivity.AppState.LIST_DEVICES) }) {
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

@Composable
fun MyApp(content: @Composable () -> Unit) {
    PicturegramTheme {
        // A surface container using the 'background' color from the theme
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}

@Composable
fun rememberWebViewWithLifecycle(submittedFormListener: () -> Unit): WebView {
    val context = ContextAmbient.current
    val webview = remember {
        object: WebView(context) {
            override fun postUrl(url: String?, postData: ByteArray?) {
                println("Post URL loading ${url}")
                super.postUrl(url, postData)
            }
        }.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    // Needed so we do not leave WebView with a browser intent
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.loadUrl("javascript:document.getElementsByName(\"submit\")[0].onclick = function() { Android.onFormSubmitted(); }");
                }
            }
            settings.apply {
                @SuppressLint("SetJavaScriptEnabled") // Need JavaScript support
                javaScriptEnabled = true
                addJavascriptInterface(WebAppInterface(submittedFormListener), "Android")
            }
        }
    }
    val lifecycleObserver = rememberWebViewLifecycleObserver(webview)
    val lifecycle = LifecycleOwnerAmbient.current.lifecycle
    onCommit(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return webview
}

class WebAppInterface(val submittedFormListener: () -> Unit) {
    @JavascriptInterface
    fun onFormSubmitted() {
        submittedFormListener()
    }
}

@Composable
fun WebViewContainer(webView: WebView) {
    AndroidView({webView})
}

@Composable
private fun rememberWebViewLifecycleObserver(webView: WebView): LifecycleEventObserver =
        remember(webView) {
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> webView.onResume()
                    Lifecycle.Event.ON_PAUSE -> webView.onPause()
                    else -> {
                        // do nothing
                    }
                }
            }
        }
