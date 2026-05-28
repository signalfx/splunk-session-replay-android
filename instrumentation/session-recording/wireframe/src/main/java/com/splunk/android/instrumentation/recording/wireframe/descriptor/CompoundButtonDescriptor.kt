package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.CompoundButton
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.wireframe.extension.buttonDrawableCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class CompoundButtonDescriptor : ButtonDescriptor() {

    override val intendedClass: Class<*>? = CompoundButton::class.java

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is CompoundButton)
            return

        result += view.buttonDrawableCompat?.getSkeleton()
    }
}
