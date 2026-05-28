package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.menu

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class ListMenuItemViewCompatDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.view.menu.ListMenuItemView".toClass()
}
