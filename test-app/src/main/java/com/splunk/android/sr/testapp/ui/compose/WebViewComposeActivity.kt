/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.splunk.android.sr.testapp.ui.compose

import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.NestedScrollView
import com.splunk.android.sr.testapp.util.sessionReplay

class WebViewComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@Composable
private fun Content() {
    Box {
        WebContent(
            url = "https://policies.google.com/terms?hl=en-US"
        )
    }
}

@Composable
private fun WebContent(modifier: Modifier = Modifier, url: String) {
    var progress: Float by remember { mutableStateOf(0f) }
    val scrollState = rememberScrollState()

    if (progress / 100f < 1f) {
        CustomProgressBar(
            progress = progress / 100
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )
    }

    AndroidView(
        factory = { context ->
            val web = WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                webChromeClient = MyWebViewClient(
                    updateProgress = {
                        progress = it.toFloat()
                    }
                )
            }

            NestedScrollView(context).apply {
                addView(web)
                web.loadUrl(url)
            }
        },
        modifier = modifier // TODO Investigate sensitivity bounds
            .height(IntrinsicSize.Max)
            .clipToBounds()
            .verticalScroll(scrollState)
            .sessionReplay(
                id = "WebView"
            )
    )
}

@Composable
fun CustomProgressBar(progress: Float) {
    val progressFraction = (progress / 100f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color.LightGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressFraction)
                .fillMaxHeight()
                .background(Color.Black)
        )
    }
}

private class MyWebViewClient(val updateProgress: (Int) -> Unit) : WebChromeClient() {
    override fun onProgressChanged(view: WebView, newProgress: Int) {
        updateProgress(newProgress)
        super.onProgressChanged(view, newProgress)
    }
}
