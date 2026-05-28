package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CheckedTextViewDescriptor

internal open class AppCompatCheckedTextViewDescriptor : CheckedTextViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatCheckedTextView".toClass()
}
