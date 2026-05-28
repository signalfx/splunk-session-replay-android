package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class ContentFrameLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.ContentFrameLayout".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
