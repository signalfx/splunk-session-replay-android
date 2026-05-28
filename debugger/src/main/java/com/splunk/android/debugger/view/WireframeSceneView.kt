package com.splunk.android.debugger.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import com.splunk.android.debugger.R
import com.splunk.android.debugger.drawer.KeyboardDrawer
import com.splunk.android.debugger.drawer.WireframeDrawer
import com.splunk.android.debugger.extension.aspectRatio
import com.splunk.android.debugger.extension.toDrawElements
import com.splunk.android.debugger.model.DrawElement
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal class WireframeSceneView(context: Context, attrs: AttributeSet? = null) : WrapContentView(context, attrs) {

    private val localCanvas = Canvas()
    private var bitmap: Bitmap? = null

    private var drawElements: List<DrawElement>? = null

    var scene: Wireframe.Frame.Scene? = null
        set(value) {
            field = value
            drawElements = value?.toDrawElements()

            requestLayout()
            invalidate()
        }

    @WireframeDrawer.Flag
    var flags: Int = WireframeDrawer.FLAG_DEFAULT
        set(value) {
            field = value
            invalidate()
        }

    var interactions: List<Interaction>? = null
        set(value) {
            field = value
            invalidate()
        }

    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.WireframeSceneView)
            flags = a.getInt(R.styleable.WireframeSceneView_wireframe_flags, flags)
            a.recycle()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        if (measuredWidth > 0 && measuredHeight > 0 && bitmap?.let { it.width != measuredWidth || it.height != measuredHeight } != false) {
            bitmap?.recycle()

            val localBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
            localCanvas.setBitmap(localBitmap)
            bitmap = localBitmap
        }
    }

    override fun getContentAspectRatio(): Float {
        return scene?.rect?.aspectRatio ?: 0f
    }

    override fun onDraw(canvas: Canvas) {
        val drawElements = drawElements ?: return
        val bitmap = bitmap ?: return
        val scene = scene ?: return

        val scale = width.toFloat() / scene.rect.width()

        localCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val save = localCanvas.save()
        localCanvas.scale(scale, scale)
        WireframeDrawer.draw(localCanvas, drawElements, flags)
        localCanvas.restoreToCount(save)

        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val sceneRect = scene.rect
        val interactions = interactions ?: return
        KeyboardDrawer.draw(canvas, sceneRect.width(), sceneRect.height(), width, height, interactions)
    }
}
