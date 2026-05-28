package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.RelativeLayout

internal open class RelativeLayoutDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = RelativeLayout::class.java
}
