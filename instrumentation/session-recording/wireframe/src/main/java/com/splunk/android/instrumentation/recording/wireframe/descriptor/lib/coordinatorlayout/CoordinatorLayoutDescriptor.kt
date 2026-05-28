package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.coordinatorlayout

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class CoordinatorLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.coordinatorlayout.widget.CoordinatorLayout".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
