package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ToggleButtonDescriptor

internal open class AppCompatToggleButtonDescriptor : ToggleButtonDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatToggleButton".toClass()
}
