package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RadioButtonDescriptor

internal open class AppCompatRadioButtonDescriptor : RadioButtonDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatRadioButton".toClass()
}
