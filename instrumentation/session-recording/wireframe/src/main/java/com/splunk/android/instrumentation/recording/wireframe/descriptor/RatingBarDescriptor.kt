package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.RatingBar
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class RatingBarDescriptor : AbsSeekBarDescriptor() {

    override val intendedClass: Class<*>? = RatingBar::class.java

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        result += view.background?.getSkeleton()

        if (view !is RatingBar)
            return

        val skeleton = view.progressDrawable?.getSkeleton() ?: return
        skeleton.rect.offset(view.paddingLeft, view.paddingTop)

        result += skeleton
    }
}
