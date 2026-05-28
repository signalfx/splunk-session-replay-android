package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.TextureView
import android.view.View
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class TextureViewDescriptor : ViewDescriptor() {

    override val intendedClass: Class<*>? = TextureView::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.SURFACE
    }

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }
}
