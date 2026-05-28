package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.snackbar

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class SnackbarSnackbarLayoutDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.snackbar.Snackbar\$SnackbarLayout".toClass()
}
