package com.splunk.android.sr.testapp.util

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.Modifier
import com.splunk.android.instrumentation.recording.interactions.compose.PointerInputObserverInjectorModifier
import com.splunk.android.instrumentation.recording.wireframe.canvas.compose.SessionReplayDrawModifier

fun randomColor(): Int {
    val hue = Math.random().toFloat() * 360f
    val saturation = (Math.random().toFloat() * 2000f + 1000f) / 10000f
    val luminance = 0.9f
    return Color.HSVToColor(floatArrayOf(hue, saturation, luminance))
}

/**
 * Session Replay [Modifier] that adds additional info into wireframe.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
fun Modifier.sessionReplay(id: String? = null, isSensitive: Boolean? = null, positionInList: Int? = null): Modifier {
    val finalId = if (id != null) "userid_$id" else null
    var modifier = this.then(SessionReplayDrawModifier(finalId, isSensitive))

    if (finalId != null)
        modifier = modifier.then(PointerInputObserverInjectorModifier(finalId, positionInList))

    println("sessionReplay: $finalId")
    return modifier
}
