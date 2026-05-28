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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.splunk.android.sr.testapp.util.sessionReplay

class DrawRecompositionComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@Preview
@Composable
private fun Content() {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .sessionReplay(id = "root")
    ) {
        for (i in 0 until 5)
            CheckboxRow(
                text = "Checkbox $i",
                modifier = Modifier.padding(vertical = 8.dp),
            )
    }
}

@Composable
private fun CheckboxRow(
    text: String,
    modifier: Modifier,
) {
    val selected = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected.value) Color(0xffefbcd5) else Color(0xff4b5267))
            .clickable {
                selected.value = !selected.value
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicText(
                text = text,
                modifier = Modifier.weight(1f),
                style = TextStyle(
                    color = if (selected.value) Color.Black else Color.White
                )
            )

            Box(
                modifier = Modifier.padding(8.dp)
            ) {
                Checkbox(
                    checked = selected.value,
                    checkedColor = Color(0xff4b5267),
                    uncheckedColor = Color.White,
                    checkmarkColor = Color.White
                )
            }
        }
    }
}

@Composable
private fun Checkbox(
    checked: Boolean,
    checkedColor: Color,
    uncheckedColor: Color,
    checkmarkColor: Color
) {
    val size = 20.dp
    val strokeWidth = 2.dp

    Canvas(
        modifier = Modifier.requiredSize(size)
    ) {
        val checkboxSize = this.size.width

        if (checked) {
            drawRoundRect(
                color = checkedColor,
                cornerRadius = CornerRadius(4.dp.toPx())
            )

            val path = Path().apply {
                moveTo(checkboxSize * 0.2f, checkboxSize * 0.5f)
                lineTo(checkboxSize * 0.4f, checkboxSize * 0.7f)
                lineTo(checkboxSize * 0.8f, checkboxSize * 0.3f)
            }

            drawPath(
                path = path,
                color = checkmarkColor,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        } else {
            drawRoundRect(
                color = uncheckedColor,
                style = Stroke(width = strokeWidth.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}
