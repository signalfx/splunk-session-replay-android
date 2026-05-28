package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass

internal open class DayPickerViewPagerDescriptor : InternalViewPagerDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.DayPickerViewPager".toClass()
}
