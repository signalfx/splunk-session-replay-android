package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.EditText

internal open class EditTextDescriptor : TextViewDescriptor() {

    override val intendedClass: Class<*>? = EditText::class.java
}
