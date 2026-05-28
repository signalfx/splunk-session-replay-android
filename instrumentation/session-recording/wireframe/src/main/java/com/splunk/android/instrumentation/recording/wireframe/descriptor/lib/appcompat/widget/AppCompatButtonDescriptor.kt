package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ButtonDescriptor

internal open class AppCompatButtonDescriptor : ButtonDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatButton".toClass()
}
