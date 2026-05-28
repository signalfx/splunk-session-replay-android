package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class ButtonBarLayoutCompatDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.ButtonBarLayout".toClass()
}
