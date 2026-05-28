package com.splunk.android.sr.testapp.ui.compose

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.os.postDelayed
import com.splunk.android.common.utils.extensions.contentView

class MeasureRecompositionComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }

        contentView?.getChildAt(0)?.setBackgroundColor(0xff00ff00.toInt())
    }
}

@Composable
private fun Content() {
    var lines by remember { mutableStateOf(2) }

    Handler(Looper.getMainLooper()).postDelayed(2000) {
        lines = 30
    }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .background(Color.Red)
    ) {
        (0..lines).forEach {
            Box(
                modifier = Modifier
                    .padding(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Blue)
                        .fillMaxWidth()
                        .height(5.dp)
                )
            }
        }
    }
}
