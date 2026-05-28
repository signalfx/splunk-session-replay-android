package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.NumberPicker

internal open class NumberPickerDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = NumberPicker::class.java

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
