package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.CheckBox

internal open class CheckBoxDescriptor : CompoundButtonDescriptor() {

    override val intendedClass: Class<*>? = CheckBox::class.java
}
