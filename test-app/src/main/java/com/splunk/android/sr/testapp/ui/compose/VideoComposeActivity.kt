package com.splunk.android.sr.testapp.ui.compose

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.util.sessionReplay

class VideoComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Content()
        }
    }
}

@Composable
private fun Content() {
    val context = LocalContext.current

    val mediaItem = MediaItem.Builder()
        .setUri(Uri.Builder().scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).path(R.raw.rainy_day.toString()).build())
        .build()

    val player = ExoPlayer.Builder(context)
        .build()

    player.setMediaItem(mediaItem)
    player.repeatMode = Player.REPEAT_MODE_ONE
    player.prepare()

    Column {
        BasicText(
            text = "Video player",
            modifier = Modifier
                .padding(
                    bottom = 10.dp
                )
        )
        DisposableEffect(
            AndroidView(
                factory = {
                    StyledPlayerView(it).also {
                        it.player = player
                    }
                },
                modifier = Modifier
                    .aspectRatio(0.764f)
                    .sessionReplay()
            )
        ) {
            onDispose { player.release() }
        }
    }
}
