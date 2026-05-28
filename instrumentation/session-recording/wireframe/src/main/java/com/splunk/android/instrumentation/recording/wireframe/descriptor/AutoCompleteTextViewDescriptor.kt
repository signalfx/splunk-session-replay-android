package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.AutoCompleteTextView

internal open class AutoCompleteTextViewDescriptor : EditTextDescriptor() {

    override val intendedClass: Class<*>? = AutoCompleteTextView::class.java
}
