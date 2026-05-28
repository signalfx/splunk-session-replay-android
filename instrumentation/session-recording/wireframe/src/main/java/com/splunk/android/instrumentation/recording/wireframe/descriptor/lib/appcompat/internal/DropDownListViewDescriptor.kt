package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ListViewDescriptor

internal open class DropDownListViewDescriptor : ListViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.DropDownListView".toClass()
}
