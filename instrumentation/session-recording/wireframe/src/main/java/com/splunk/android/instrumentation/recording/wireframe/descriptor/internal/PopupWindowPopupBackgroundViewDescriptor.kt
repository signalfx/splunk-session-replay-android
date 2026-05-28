package com.splunk.android.instrumentation.recording.wireframe.descriptor.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

internal open class PopupWindowPopupBackgroundViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "android.widget.PopupWindow\$PopupBackgroundView".toClass()
}
