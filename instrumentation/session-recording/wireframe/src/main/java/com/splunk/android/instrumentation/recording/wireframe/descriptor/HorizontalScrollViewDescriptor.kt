package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.graphics.Point
import android.view.View
import android.widget.HorizontalScrollView

internal open class HorizontalScrollViewDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = HorizontalScrollView::class.java

    override fun getScrollOffset(view: View): Point {
        return Point(view.scrollX, view.scrollY)
    }
}
