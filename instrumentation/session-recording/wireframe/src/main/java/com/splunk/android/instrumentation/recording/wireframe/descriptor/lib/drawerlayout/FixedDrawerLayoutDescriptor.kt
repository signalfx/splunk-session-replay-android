package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.drawerlayout

import com.splunk.android.common.utils.extensions.toClass

internal open class FixedDrawerLayoutDescriptor : DrawerLayoutDescriptor() {

    override val intendedClass: Class<*>? = "androidx.drawerlayout.widget.FixedDrawerLayout".toClass()
}
