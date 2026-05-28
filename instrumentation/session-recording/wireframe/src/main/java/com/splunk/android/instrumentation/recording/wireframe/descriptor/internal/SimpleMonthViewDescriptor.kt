package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewDescriptor

internal open class SimpleMonthViewDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.SimpleMonthView".toClass()
}
