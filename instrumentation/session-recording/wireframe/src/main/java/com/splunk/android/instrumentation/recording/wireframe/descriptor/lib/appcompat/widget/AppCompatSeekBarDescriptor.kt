package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SeekBarDescriptor

internal open class AppCompatSeekBarDescriptor : SeekBarDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatSeekBar".toClass()
}
