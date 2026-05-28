package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.RadioButton

internal open class RadioButtonDescriptor : CompoundButtonDescriptor() {

    override val intendedClass: Class<*>? = RadioButton::class.java
}
