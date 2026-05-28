package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SpinnerDescriptor

internal open class AppCompatSpinnerDescriptor : SpinnerDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatSpinner".toClass()
}
