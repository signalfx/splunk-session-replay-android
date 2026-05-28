package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.navigation

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.ScrimInsetsFrameLayoutDescriptor

internal open class NavigationViewDescriptor : ScrimInsetsFrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.navigation.NavigationView".toClass()
}
