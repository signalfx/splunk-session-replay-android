package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass

internal open class AlertDialogLayoutCompatDescriptor : LinearLayoutCompatDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AlertDialogLayout".toClass()
}
