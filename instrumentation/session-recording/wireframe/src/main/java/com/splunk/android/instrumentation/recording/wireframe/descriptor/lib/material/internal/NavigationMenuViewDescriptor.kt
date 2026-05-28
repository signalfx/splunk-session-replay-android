package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.recyclerview.RecyclerViewDescriptor

internal open class NavigationMenuViewDescriptor : RecyclerViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.internal.NavigationMenuView".toClass()
}
