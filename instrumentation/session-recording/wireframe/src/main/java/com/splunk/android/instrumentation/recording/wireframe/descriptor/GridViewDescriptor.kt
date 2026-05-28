package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.GridView

internal open class GridViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = GridView::class.java
}
