package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.SurfaceView
import android.view.View
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class SurfaceViewDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = SurfaceView::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.SURFACE
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun isDrawDeterministic(view: View): Boolean {
        return false
    }
}
