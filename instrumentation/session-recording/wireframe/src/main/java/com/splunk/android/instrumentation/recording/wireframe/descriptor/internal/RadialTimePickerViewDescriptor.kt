package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class RadialTimePickerViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.RadialTimePickerView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
