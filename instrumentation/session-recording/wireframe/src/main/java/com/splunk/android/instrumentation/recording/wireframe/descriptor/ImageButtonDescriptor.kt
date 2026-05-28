package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.ImageButton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class ImageButtonDescriptor : ImageViewDescriptor() {

    override val intendedClass: Class<*>? = ImageButton::class.java

    override fun getType(view: View): Window.View.Type {
        return Window.View.Type.BUTTON
    }
}
