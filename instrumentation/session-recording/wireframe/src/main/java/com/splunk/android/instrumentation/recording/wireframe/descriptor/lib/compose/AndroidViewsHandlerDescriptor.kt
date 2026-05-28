package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class AndroidViewsHandlerDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.compose.ui.platform.AndroidViewsHandler".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
