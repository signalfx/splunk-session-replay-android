package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textview

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatTextViewDescriptor

internal open class MaterialTextViewDescriptor : AppCompatTextViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.textview.MaterialTextView".toClass()
}
