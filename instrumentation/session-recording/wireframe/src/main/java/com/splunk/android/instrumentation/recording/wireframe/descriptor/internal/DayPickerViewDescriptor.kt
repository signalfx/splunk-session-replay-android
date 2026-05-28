package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

internal open class DayPickerViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.DayPickerView".toClass()
}
