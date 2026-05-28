package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.ProgressBar
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas
import com.splunk.android.instrumentation.recording.wireframe.extension.LayoutDirection
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.extension.isDrawDeterministic
import com.splunk.android.instrumentation.recording.wireframe.extension.layoutDirectionCompat
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class ProgressBarDescriptor : ViewDescriptor() {

    private val canvas = SkeletonCanvas()

    override val intendedClass: Class<*>? = ProgressBar::class.java

    override fun getType(view: View): Window.View.Type? {
        return if (view is ProgressBar && view.isIndeterminate)
            Window.View.Type.SPINNING_WHEEL
        else
            Window.View.Type.PROGRESS
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is ProgressBar)
            return

        getProgressSkeleton(view, result)
    }

    override fun isDrawDeterministic(view: View): Boolean {
        return super.isDrawDeterministic(view) && view is ProgressBar && view.indeterminateDrawable?.isDrawDeterministic != false && view.progressDrawable?.isDrawDeterministic != false
    }

    private fun getProgressSkeleton(view: ProgressBar, result: MutableList<Window.View.Skeleton>) {
        if (view.isIndeterminate) {
            val skeleton = view.indeterminateDrawable?.getSkeleton() ?: return
            skeleton.rect.offset(view.paddingLeft, view.paddingTop)

            result += skeleton
        } else {
            val drawable = view.progressDrawable ?: return
            val saveCount = canvas.save()

            if (view.layoutDirectionCompat == LayoutDirection.RTL)
                canvas.scale(-1f, 1f)

            canvas.clipRect(drawable.bounds)
            drawable.draw(canvas)
            canvas.restoreToCount(saveCount)

            for (skeleton in canvas.skeletons) {
                skeleton.rect.offset(view.paddingLeft, view.paddingTop)
                result += skeleton
            }

            canvas.skeletons.clear()
        }
    }
}
