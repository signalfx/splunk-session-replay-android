package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.snackbar

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class SnackbarContentLayoutDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.snackbar.SnackbarContentLayout".toClass()
}
