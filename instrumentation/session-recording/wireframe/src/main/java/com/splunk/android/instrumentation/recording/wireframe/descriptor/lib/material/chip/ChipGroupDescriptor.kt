package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.chip

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.FlowLayoutDescriptor

internal open class ChipGroupDescriptor : FlowLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.chip.ChipGroup".toClass()
}
