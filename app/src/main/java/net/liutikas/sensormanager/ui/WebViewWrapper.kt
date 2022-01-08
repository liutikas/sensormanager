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

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun WebViewContainer(webView: WebView) {
    AndroidView({ webView })
}

@Composable
fun rememberWebViewWithLifecycle(submittedFormListener: () -> Unit): WebView {
    val context = LocalContext.current
    val webview = remember {
        WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    // Needed so we do not leave WebView with a browser intent
                    return super.shouldOverrideUrlLoading(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.loadUrl(ONCLICK_INJECTOR)
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return webview
}

private const val ONCLICK_INJECTOR = "javascript:document.getElementsByName(\"submit\")[0].onclick = function() { Android.onFormSubmitted(); }"

internal class WebAppInterface(val submittedFormListener: () -> Unit) {
    @JavascriptInterface
    fun onFormSubmitted() {
        submittedFormListener()
    }
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
