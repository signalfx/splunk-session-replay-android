package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.TableRow

internal open class TableRowDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = TableRow::class.java
}
