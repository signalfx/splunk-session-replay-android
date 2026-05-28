package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class FitWindowsFrameLayoutDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.FitWindowsFrameLayout".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
