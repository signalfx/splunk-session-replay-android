package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.VideoView
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class VideoViewDescriptor : SurfaceViewDescriptor() {

    override val intendedClass: Class<*>? = VideoView::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.VIDEO
    }
}
