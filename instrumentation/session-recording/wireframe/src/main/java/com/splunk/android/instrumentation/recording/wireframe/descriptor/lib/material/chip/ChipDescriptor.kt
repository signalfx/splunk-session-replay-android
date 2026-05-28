package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.chip

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatCheckBoxDescriptor

internal open class ChipDescriptor : AppCompatCheckBoxDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.chip.Chip".toClass()
}
