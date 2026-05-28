package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class FitWindowsLinearLayoutDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.FitWindowsLinearLayout".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
