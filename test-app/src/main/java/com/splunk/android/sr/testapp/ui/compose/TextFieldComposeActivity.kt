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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
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

class TextFieldComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 500)
@Composable
private fun Content() {
    Column(
        modifier = Modifier.padding(20.dp),
    ) {
        BasicText(
            modifier = Modifier.fillMaxWidth(),
            text = "Single line",
            style = TextStyle(color = Color.Black, fontSize = 14.sp)
        )

        MyTextField(
            singleLine = true
        )

        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            text = "Multi line - fixed height",
            style = TextStyle(color = Color.Black, fontSize = 14.sp)
        )

        MyTextField(
            modifier = Modifier.height(100.dp),
            singleLine = false
        )

        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            text = "Multi line - wrap height",
            style = TextStyle(color = Color.Black, fontSize = 14.sp)
        )

        MyTextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = false
        )
    }
}

@Composable
private fun MyTextField(
    modifier: Modifier = Modifier,
    singleLine: Boolean
) {
    val string = remember { mutableStateOf("") }

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
                        text = "Enter a text",
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
