package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TextViewDescriptor

internal open class NumericTextViewDescriptor : TextViewDescriptor() {

    override val intendedClass: Class<*>? = "com.android.internal.widget.NumericTextView".toClass()
}
