package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal.DropDownListViewDescriptor

internal open class MenuPopupWindowMenuDropDownListViewCompatDescriptor : DropDownListViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.MenuPopupWindow\$MenuDropDownListView".toClass()
}
