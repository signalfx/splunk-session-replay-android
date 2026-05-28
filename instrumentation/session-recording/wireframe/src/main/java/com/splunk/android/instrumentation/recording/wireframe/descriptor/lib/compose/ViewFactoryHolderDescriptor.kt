package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose

import android.view.View
import com.splunk.android.common.utils.extensions.toClass

internal open class ViewFactoryHolderDescriptor : AndroidViewHolderDescriptor() {

    override val intendedClass: Class<*>? = "androidx.compose.ui.viewinterop.ViewFactoryHolder".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
