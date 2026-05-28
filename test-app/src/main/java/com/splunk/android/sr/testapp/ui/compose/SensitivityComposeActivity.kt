package com.splunk.android.sr.testapp.ui.compose

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splunk.android.sr.testapp.util.sessionReplay

class SensitivityComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun Content() {
    var redrawProperty by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { redrawProperty = !redrawProperty }
            .background(color = Color.Red)
    ) {
        var isVisible by remember { mutableStateOf(true) }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .clickable { isVisible = !isVisible }
                .padding(all = 10.dp)
        ) {
            BasicText(
                text = if (isVisible) "Hide transparent layer" else "Show transparent layer",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                text = "Sensitive",
                style = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier.sessionReplay(
                    id = "sensitive_text",
                    isSensitive = true
                )
            )

            Box(modifier = Modifier.padding(4.dp))

            BasicText(
                text = "Not sensitive",
                style = TextStyle(color = Color.White, fontSize = 16.sp),
                modifier = Modifier.sessionReplay(
                    id = "non_sensitive_text",
                    isSensitive = false
                )
            )
        }

        if (isVisible)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.5f }
                    .background(Color.Blue)
            )
    }
}
