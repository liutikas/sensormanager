package net.liutikas.picturegram

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.onDispose
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.platform.LifecycleOwnerAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.ui.tooling.preview.Preview
import net.liutikas.picturegram.ui.PicturegramTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp {
                MyScreenContent()
            }
            handleWifi(this)
        }

    }
}

@Composable
private fun MyScreenContent() {
    Column(modifier = Modifier.fillMaxHeight()) {
        val webview = rememberWebViewWithLifecycle()
        Column(modifier = Modifier.weight(1f)) {
            WebViewContainer(webview)
        }
        Divider(color = Color.Transparent, thickness = 32.dp)
        Button(onClick = { webview.loadUrl("http://192.168.4.1/config") }) {
            Text("Load the URL")
        }
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

@Composable
fun rememberWebViewWithLifecycle(): WebView {
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
                    println("Override URL loading ${request?.url}")
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    println("Page started $url")
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // document.getElementsByName("submit")[0].onclick = function() { alert("testing");}
                    view?.loadUrl("javascript:document.getElementsByName(\"submit\")[0].onclick = function() { Android.showToast(\"foo\"); }");
                }
            }
            settings.apply {
                javaScriptEnabled = true
                addJavascriptInterface(WebAppInterface(context), "Android")
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

class WebAppInterface(private val mContext: Context) {
    /** Show a toast from the web page  */
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
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
                    } //throw IllegalStateException()
                }
            }
        }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApp {
        MyScreenContent()
    }
}
