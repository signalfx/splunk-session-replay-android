package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AnimatedStateListDrawable
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build

internal val Drawable.isDrawDeterministic: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && this !is AnimatedVectorDrawable && this !is AnimatedStateListDrawable

internal fun Drawable.toBitmap(): Bitmap {
    val bounds = bounds
    val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.translate(-bounds.left, -bounds.top)
    draw(canvas)
    return bitmap
}
