package com.splunk.android.instrumentation.recording.screenshot.image

import android.graphics.Bitmap
import android.view.SurfaceView
import android.view.View
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal interface ImageCopy {

    fun copyWindow(view: View, windowDescription: Wireframe.Frame.Scene.Window, viewDescription: Wireframe.Frame.Scene.Window.View, bitmap: Bitmap)

    fun copySurface(view: SurfaceView, bitmap: Bitmap)
}
