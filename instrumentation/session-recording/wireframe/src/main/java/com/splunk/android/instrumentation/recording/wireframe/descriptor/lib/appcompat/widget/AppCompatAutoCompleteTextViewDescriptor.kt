package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.AutoCompleteTextViewDescriptor

internal open class AppCompatAutoCompleteTextViewDescriptor : AutoCompleteTextViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatAutoCompleteTextView".toClass()
}
