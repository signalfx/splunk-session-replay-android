package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.RadioGroup

internal open class RadioGroupDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = RadioGroup::class.java
}
