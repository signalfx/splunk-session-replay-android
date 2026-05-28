package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.CheckedTextView

internal open class CheckedTextViewDescriptor : TextViewDescriptor() {

    override val intendedClass: Class<*>? = CheckedTextView::class.java
}
