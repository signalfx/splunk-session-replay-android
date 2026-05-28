package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.checkbox

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatCheckBoxDescriptor

internal open class MaterialCheckBoxDescriptor : AppCompatCheckBoxDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.checkbox.MaterialCheckBox".toClass()
}
