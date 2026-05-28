package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.radiobutton

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatRadioButtonDescriptor

internal open class MaterialRadioButtonDescriptor : AppCompatRadioButtonDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.radiobutton.MaterialRadioButton".toClass()
}
