package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageViewDescriptor

internal open class ActionMenuPresenterOverflowMenuButtonDescriptor : AppCompatImageViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.ActionMenuPresenter\$OverflowMenuButton".toClass()
}
