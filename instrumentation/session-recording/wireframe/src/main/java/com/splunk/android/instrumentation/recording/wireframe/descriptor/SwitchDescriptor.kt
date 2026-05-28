package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.Switch
import com.splunk.android.common.utils.extensions.plusAssign
import com.splunk.android.instrumentation.recording.wireframe.extension.getSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class SwitchDescriptor : CompoundButtonDescriptor() {

    override val intendedClass: Class<*>? = Switch::class.java

    override fun getSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getSkeletons(view, isSensitive, result)

        if (view !is Switch)
            return

        result += view.trackDrawable?.getSkeleton()
        result += view.thumbDrawable?.getSkeleton()
    }
}
