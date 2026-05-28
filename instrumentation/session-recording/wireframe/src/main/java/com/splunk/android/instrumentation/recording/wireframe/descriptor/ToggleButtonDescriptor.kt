package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.ToggleButton

internal open class ToggleButtonDescriptor : CompoundButtonDescriptor() {

    override val intendedClass: Class<*>? = ToggleButton::class.java
}
