package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass

internal open class ActionMenuViewCompatDescriptor : LinearLayoutCompatDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.ActionMenuView".toClass()
}
