package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.TimePicker
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* FIXME
 *  - Wrong skeleton for AM/PM
 */
internal open class TimePickerDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = TimePicker::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.TIME_PICKER
    }
}
