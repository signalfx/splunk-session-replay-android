package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.LinearLayout

internal open class LinearLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = LinearLayout::class.java
}
