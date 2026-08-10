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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splunk.android.sr.testapp.util.sessionReplay

class TextFieldComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

// Texts are shared with TextFieldComposeSensitivityTest, which looks them up in the extracted wireframe.
internal const val LABEL_SINGLE_LINE = "Single line"
internal const val LABEL_MULTI_LINE_FIXED_HEIGHT = "Multi line - fixed height"
internal const val LABEL_MULTI_LINE_WRAP_HEIGHT = "Multi line - wrap height"
internal const val LABEL_NOT_SENSITIVE_ANCESTOR = "Not sensitive ancestor - stays sensitive"
internal const val LABEL_NOT_SENSITIVE_TEXT_FIELD = "Not sensitive text field"

internal const val PLACEHOLDER = "Enter a text"
internal const val VALUE_IN_NOT_SENSITIVE_ANCESTOR = "Value in not sensitive ancestor"
internal const val VALUE_IN_NOT_SENSITIVE_TEXT_FIELD = "Value in not sensitive text field"

@Preview(showBackground = true, widthDp = 320, heightDp = 500)
@Composable
private fun Content() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Label(
            text = LABEL_SINGLE_LINE
        )

        MyTextField(
            singleLine = true
        )

        Label(text = LABEL_MULTI_LINE_FIXED_HEIGHT)

        MyTextField(
            modifier = Modifier.height(100.dp),
            singleLine = false
        )

        Label(text = LABEL_MULTI_LINE_WRAP_HEIGHT)

        MyTextField(
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = false
        )

        Label(text = LABEL_NOT_SENSITIVE_ANCESTOR)

        Column(
            modifier = Modifier
                .sessionReplay(isSensitive = false)
        ) {
            MyTextField(
                singleLine = true,
                value = VALUE_IN_NOT_SENSITIVE_ANCESTOR
            )
        }

        Label(text = LABEL_NOT_SENSITIVE_TEXT_FIELD)

        MyTextField(
            modifier = Modifier
                .sessionReplay(isSensitive = false),
            singleLine = true,
            value = VALUE_IN_NOT_SENSITIVE_TEXT_FIELD
        )
    }
}

@Composable
private fun Label(text: String) {
    BasicText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
            .sessionReplay(isSensitive = false),
        text = text,
        style = TextStyle(color = Color.Black, fontSize = 14.sp)
    )
}

@Composable
private fun MyTextField(
    modifier: Modifier = Modifier,
    singleLine: Boolean,
    value: String = ""
) {
    val string = remember { mutableStateOf(value) }

    BasicTextField(
        value = string.value,
        onValueChange = { string.value = it },
        singleLine = singleLine,
        textStyle = TextStyle(
            color = Color.Black,
            fontSize = 16.sp
        ),
        cursorBrush = SolidColor(Color.Black),
        modifier = modifier
            .padding(top = 20.dp)
            .border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color.Transparent)
            .fillMaxWidth()
            .padding(16.dp),
        decorationBox = { innerTextField ->
            Box {
                if (string.value.isEmpty()) {
                    BasicText(
                        text = PLACEHOLDER,
                        style = TextStyle(
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}
