package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.ListView

internal open class ListViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = ListView::class.java
}
