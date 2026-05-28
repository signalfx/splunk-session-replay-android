package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ProgressBarDescriptor

internal open class ContentLoadingProgressBarDescriptor : ProgressBarDescriptor() {

    override val intendedClass: Class<*>? = "androidx.core.widget.ContentLoadingProgressBar".toClass()
}
