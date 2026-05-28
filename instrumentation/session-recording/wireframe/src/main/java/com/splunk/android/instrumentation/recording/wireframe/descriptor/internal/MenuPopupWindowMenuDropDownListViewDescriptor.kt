package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal.DropDownListViewDescriptor

internal open class MenuPopupWindowMenuDropDownListViewDescriptor : DropDownListViewDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.MenuPopupWindow\$MenuDropDownListView".toClass()
}
