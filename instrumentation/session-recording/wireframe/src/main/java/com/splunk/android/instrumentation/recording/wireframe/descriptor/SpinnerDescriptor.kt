package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.Spinner

internal open class SpinnerDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = Spinner::class.java
}
