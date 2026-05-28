package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.view.View
import android.widget.CalendarView
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

internal open class CalendarViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = CalendarView::class.java

    override fun getType(view: View): Window.View.Type? {
        return Window.View.Type.DATE_PICKER
    }
}
