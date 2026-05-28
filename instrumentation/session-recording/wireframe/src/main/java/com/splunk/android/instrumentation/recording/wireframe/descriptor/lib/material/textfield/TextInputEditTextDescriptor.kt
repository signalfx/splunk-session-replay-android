package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textfield

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatEditTextDescriptor

internal open class TextInputEditTextDescriptor : AppCompatEditTextDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.textfield.TextInputEditText".toClass()
}
