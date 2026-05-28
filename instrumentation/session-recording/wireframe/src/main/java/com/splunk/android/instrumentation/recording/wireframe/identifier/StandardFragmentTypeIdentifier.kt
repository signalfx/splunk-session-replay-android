package com.splunk.android.instrumentation.recording.wireframe.identifier

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.extension.isInheritedBy
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* TODO
 *  - Stop extraction
 *  - Huawei Map Fragment
 *  - Open street maps
 */
internal class StandardFragmentTypeIdentifier : FragmentTypeIdentifier {

    override fun identify(fragmentClass: Class<out Any>): Window.View.Type? {
        if (GOOGLE_MAPS_PACKAGE?.isInheritedBy(fragmentClass) == true)
            return Window.View.Type.MAP

        return null
    }

    private companion object {
        val GOOGLE_MAPS_PACKAGE = "com.google.android.gms.maps.SupportMapFragment".toClass()
    }
}
