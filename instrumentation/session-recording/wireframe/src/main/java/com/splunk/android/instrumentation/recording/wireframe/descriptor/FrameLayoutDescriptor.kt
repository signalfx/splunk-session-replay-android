package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.FrameLayout

internal open class FrameLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = FrameLayout::class.java
}
