package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.menu

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatTextViewDescriptor

internal open class ActionMenuItemViewDescriptor : AppCompatTextViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.view.menu.ActionMenuItemView".toClass()
}
