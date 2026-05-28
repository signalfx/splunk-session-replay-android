package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.EditTextDescriptor

internal open class AppCompatEditTextDescriptor : EditTextDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatEditText".toClass()
}
