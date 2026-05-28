package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.gms

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* MARK
 *  - Compatible with com.google.android.gms:play-services-maps:18.0.2
 */
internal open class MapViewDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.gms.maps.MapView".toClass()

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.MAP
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
