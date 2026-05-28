package com.splunk.android.instrumentation.recording.wireframe.descriptor

import com.splunk.android.common.utils.extensions.toClass

internal open class ActionMenuViewDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.ActionMenuView".toClass() // API 21+
}
