package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Point
import android.os.Build
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.invoke
import com.splunk.android.instrumentation.recording.wireframe.extension.forEachSkeleton
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawDeterministic
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.stats.StatsCollector

internal open class TextViewDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = TextView::class.java

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is TextView)
            return

        getCompoundDrawablesSkeletons(view, result)

        if (view.text.isEmpty())
            getHintSkeletons(view, isSensitive, result)
        else
            getTextSkeletons(view, isSensitive, result)
    }

    override fun getType(view: View): Window.View.Type? {
        return if (view.isClickable)
            Window.View.Type.BUTTON
        else
            Window.View.Type.TEXT
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getScrollOffset(view: View): Point? {
        return if (view is TextView && isHorizontallyScrollable(view))
            Point(view.scrollX, view.scrollY)
        else
            null
    }

    override fun isDrawDeterministic(view: View): Boolean {
        return super.isDrawDeterministic(view) && view is TextView && view.compoundDrawables.all { it?.isDrawDeterministic != false }
    }

    private fun isHorizontallyScrollable(view: TextView): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            view.isHorizontallyScrollable
        else
            view.layout?.width == VERY_WIDE
    }

    private fun getCompoundDrawablesSkeletons(view: TextView, result: MutableList<Window.View.Skeleton>) {
        val drawables = view.compoundDrawables

        for (i in drawables.indices) {
            val drawable = drawables[i] ?: continue
            val skeleton = drawable.getSkeleton() ?: continue

            val drawableLeft: Int
            val drawableTop: Int

            when (i) {
                DRAWABLE_LEFT_INDEX -> {
                    val contentCenterY = (view.height - view.compoundPaddingTop - view.compoundPaddingBottom) / 2
                    val drawableCenterY = view.compoundPaddingTop + contentCenterY
                    val drawableHeightHalf = skeleton.rect.height() / 2
                    val left = view.paddingLeft

                    drawableLeft = left
                    drawableTop = drawableCenterY - drawableHeightHalf
                }
                DRAWABLE_TOP_INDEX -> {
                    val contentCenterX = (view.width - view.compoundPaddingLeft - view.compoundPaddingRight) / 2
                    val drawableCenterX = view.compoundPaddingLeft + contentCenterX
                    val drawableWidthHalf = skeleton.rect.width() / 2
                    val top = view.paddingTop

                    drawableLeft = drawableCenterX - drawableWidthHalf
                    drawableTop = top
                }
                DRAWABLE_RIGHT_INDEX -> {
                    val contentCenterY = (view.height - view.compoundPaddingTop - view.compoundPaddingBottom) / 2
                    val drawableCenterY = view.compoundPaddingTop + contentCenterY
                    val drawableHeightHalf = skeleton.rect.height() / 2
                    val right = view.width - view.paddingRight

                    drawableLeft = right - skeleton.rect.width()
                    drawableTop = drawableCenterY - drawableHeightHalf
                }
                DRAWABLE_BOTTOM_INDEX -> {
                    val contentCenterX = (view.width - view.compoundPaddingLeft - view.compoundPaddingRight) / 2
                    val drawableCenterX = view.compoundPaddingLeft + contentCenterX
                    val drawableWidthHalf = skeleton.rect.width() / 2
                    val bottom = view.height - view.paddingBottom

                    drawableLeft = drawableCenterX - drawableWidthHalf
                    drawableTop = bottom - skeleton.rect.height()
                }
                else -> continue
            }

            skeleton.rect.offset(drawableLeft, drawableTop)
            result += skeleton
        }
    }

    private fun getTextSkeletons(view: TextView, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        StatsCollector.measureTextsTime {
            val layout = view.layout ?: return
            getLayoutSkeletons(view, layout, isSensitive, result)
        }
    }

    private fun getHintSkeletons(view: TextView, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        StatsCollector.measureTextsTime {
            try {
                val layout = view.invoke<Layout>("getHintLayout") ?: return
                getLayoutSkeletons(view, layout, isSensitive, result)
            } catch (e: Exception) {
                Logger.e1(TAG, "getHintSkeletons", e)
            }
        }
    }

    private fun getLayoutSkeletons(view: TextView, layout: Layout, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        val horizontalScrollOffset = if (isHorizontallyScrollable(view)) view.scrollX else 0

        val contentLeft = view.compoundPaddingLeft - horizontalScrollOffset
        var contentTop = view.compoundPaddingTop

        when (view.gravity and ((Gravity.AXIS_PULL_BEFORE or Gravity.AXIS_PULL_AFTER) shl Gravity.AXIS_Y_SHIFT)) {
            0 -> // CENTER_VERTICAL
                contentTop += (view.height - view.compoundPaddingTop - view.compoundPaddingBottom - layout.height) / 2
            Gravity.AXIS_PULL_AFTER shl Gravity.AXIS_Y_SHIFT -> // BOTTOM
                contentTop += view.height - view.compoundPaddingTop - view.compoundPaddingBottom - layout.height
        }

        layout.forEachSkeleton(!isSensitive, view.maxLines) {
            it.rect.offset(contentLeft, contentTop)
            result += it
        }
    }

    private companion object {
        const val TAG = "TextViewDescriptor"

        const val DRAWABLE_LEFT_INDEX = 0
        const val DRAWABLE_TOP_INDEX = 1
        const val DRAWABLE_RIGHT_INDEX = 2
        const val DRAWABLE_BOTTOM_INDEX = 3

        const val VERY_WIDE = 1024 * 1024 // TextView private constant
    }
}
