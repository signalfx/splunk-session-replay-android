package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.floatingactionbutton

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageButtonDescriptor

internal open class FloatingActionButtonDescriptor : ImageButtonDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.floatingactionbutton.FloatingActionButton".toClass()
}
