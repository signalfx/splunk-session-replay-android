package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal

import com.splunk.android.common.utils.extensions.toClass

internal open class NavigationMenuItemViewDescriptor : ForegroundLinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.internal.NavigationMenuItemView".toClass()
}
