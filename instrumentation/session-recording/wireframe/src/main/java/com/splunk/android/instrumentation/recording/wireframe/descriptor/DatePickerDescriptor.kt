package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.DatePicker

internal open class DatePickerDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = DatePicker::class.java
}
