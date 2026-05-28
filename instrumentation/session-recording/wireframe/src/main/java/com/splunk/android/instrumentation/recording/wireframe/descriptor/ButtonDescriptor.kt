package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.Button
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class ButtonDescriptor : TextViewDescriptor() {

    override val intendedClass: Class<*>? = Button::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.BUTTON
    }
}
