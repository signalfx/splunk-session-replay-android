package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.button

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatButtonDescriptor

internal open class MaterialButtonDescriptor : AppCompatButtonDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.button.MaterialButton".toClass()
}
