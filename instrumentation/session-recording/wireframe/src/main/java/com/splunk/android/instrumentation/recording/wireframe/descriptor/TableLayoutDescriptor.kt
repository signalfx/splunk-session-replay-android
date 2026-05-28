package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.TableLayout

internal open class TableLayoutDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = TableLayout::class.java
}
