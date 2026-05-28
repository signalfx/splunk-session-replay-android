package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class AlertDialogLayoutDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.android.internal.widget.AlertDialogLayout".toClass()
}
