package com.splunk.android.instrumentation.recording.wireframe.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build

internal class SwSafeCanvas : Canvas {

    constructor() : super()

    constructor(bitmap: Bitmap) : super(bitmap)

    override fun drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint?) {
        super.drawBitmap(ensureSwBitmap(bitmap), left, top, paint)
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: RectF, paint: Paint?) {
        super.drawBitmap(ensureSwBitmap(bitmap), src, dst, paint)
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: Rect, paint: Paint?) {
        super.drawBitmap(ensureSwBitmap(bitmap), src, dst, paint)
    }

    override fun drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint?) {
        super.drawBitmap(ensureSwBitmap(bitmap), matrix, paint)
    }

    override fun drawBitmapMesh(bitmap: Bitmap, meshWidth: Int, meshHeight: Int, verts: FloatArray, vertOffset: Int, colors: IntArray?, colorOffset: Int, paint: Paint?) {
        super.drawBitmapMesh(ensureSwBitmap(bitmap), meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint)
    }

    private fun ensureSwBitmap(bitmap: Bitmap): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bitmap.config == Bitmap.Config.HARDWARE)
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        else
            bitmap
    }
}
