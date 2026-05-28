package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TextViewDescriptor

internal open class AppCompatTextViewDescriptor : TextViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatTextView".toClass()
}
