package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.tabs

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor

internal open class TabLayoutTabViewDescriptor : LinearLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.tabs.TabLayout\$TabView".toClass()
}
