package com.splunk.android.instrumentation.recording.wireframe.stats

import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.TextView
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton

/**
 * Debug statistics
 *
 * @param totalTime Total wireframe extraction time in milliseconds.
 * @param windowCount Number of redrawn [Wireframe.Frame.Scene.Window].
 * @param generalDrawablesTime Extraction time of [Drawable]s that are not [ColorDrawable] or [BitmapDrawable] in milliseconds.
 * @param generalDrawablesCount Number of [Drawable] that are not [ColorDrawable] or [BitmapDrawable] that were extracted.
 * @param textsTime Extraction time of all texts (usually [TextView]) in milliseconds.
 * @param textsCount Number of texts (usually [TextView]) that were extracted.
 * @param canvasTime Extraction time of all usually unknown [View]s in milliseconds.
 * @param canvasCount Number of [View] that were extracted by [Canvas].
 * @param canvasSkeletonsCount Number of [Skeleton] that were extracted from usually unknown [View]s.
 *
 * @see WireframeExtractor.extract
 */
data class WireframeStats internal constructor(
    val totalTime: Float,
    val windowCount: Int,
    val generalDrawablesTime: Float,
    val generalDrawablesCount: Int,
    val textsTime: Float,
    val textsCount: Int,
    val canvasTime: Float,
    val canvasCount: Int,
    val canvasSkeletonsCount: Int
) {

    val othersTime: Float = totalTime - generalDrawablesTime - textsTime - canvasTime

    companion object
}
