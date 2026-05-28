package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.bottomnavigation

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class BottomNavigationMenuViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.bottomnavigation.BottomNavigationMenuView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
