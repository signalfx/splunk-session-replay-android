package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.SeekBar

internal open class SeekBarDescriptor : AbsSeekBarDescriptor() {

    override val intendedClass: Class<*>? = SeekBar::class.java
}
