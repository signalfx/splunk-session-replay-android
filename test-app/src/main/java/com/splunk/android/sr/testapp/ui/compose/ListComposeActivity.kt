package com.splunk.android.sr.testapp.ui.compose

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.util.sessionReplay
import com.splunk.android.sr.testapp.view.BarChartView
import com.splunk.android.sr.testapp.view.CanvasElementsView

class ListComposeActivity : ComponentActivity() {

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TestList()
        }
    }
}

@Composable
private fun TestList(items: List<Int> = List(20) { it }) {
    LazyColumn(
        modifier = Modifier
            /*.sessionReplay( // FIXME Modifier here breaks layout
                id = "container"
            )*/
            .fillMaxWidth()
            /*.padding(
                vertical = 40.dp // FIXME Content in AndroidView isn't clipped
            )*/,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = items) { item ->
            Item(item = item)
        }
    }
}

@Composable
private fun Item(item: Int) {
    val cardShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 0.dp,
        bottomStart = 10.dp,
        bottomEnd = 10.dp
    )

    Box(
        modifier = Modifier
            .padding(
                vertical = 4.dp,
                horizontal = 8.dp
            )
            .shadow( // FIXME Missing in wireframe on Android 15
                elevation = 10.dp,
                shape = cardShape,
                clip = false
            )
            .background(
                color = Color.White,
                shape = cardShape
            )
            .clip(cardShape)
    ) {
        Content(item)
    }
}

@Composable
private fun Content(item: Int) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(12.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .size(42.dp)
                        .border(1.5.dp, Color.Red, CircleShape)
                        .border(3.dp, Color.Blue, CircleShape)
                        .clip(CircleShape),
                    painter = painterResource(
                        id = R.drawable.ic_bitmap_sample_1
                    ),
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
                BasicText(
                    text = "Hello, ",
                    modifier = Modifier
                        .padding(start = 15.dp)
                )
                BasicText(
                    text = item.toString(),
                    style = TextStyle(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            if (expanded) {
                BasicText(
                    text = "Composem ipsum color sit lazy, padding theme elit, sed do bouncy. ".repeat(4),
                    modifier = Modifier
                        .sessionReplay(
                            id = "text",
                            isSensitive = false
                        )
                        .padding(
                            top = 16.dp
                        )
                )

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(
                            top = 20.dp
                        )
                        .sessionReplay(
                            id = "BarChartView",
                            isSensitive = true
                        ),
                    factory = ::BarChartView
                )

                AndroidView( // FIXME Open the first item then sencond, this element in the second item is not visible in wireframe
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight() // FIXME .height(200.dp) broke layout when there is no other item
                        .padding(
                            top = 20.dp,
                            bottom = 20.dp
                        )
                        .sessionReplay(
                            id = "CanvasElementsView",
                            isSensitive = false
                        ),
                    factory = ::CanvasElementsView
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 9.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable {
                    expanded = !expanded
                }
                .sessionReplay(
                    id = "expand button $item"
                )
        ) {
            Image(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Show less" else "Show more",
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}
