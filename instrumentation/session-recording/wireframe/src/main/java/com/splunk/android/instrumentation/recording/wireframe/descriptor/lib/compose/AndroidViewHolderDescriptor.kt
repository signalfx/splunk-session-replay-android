package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class AndroidViewHolderDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "androidx.compose.ui.viewinterop.AndroidViewHolder".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
