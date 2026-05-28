package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.ScrollView

internal open class ScrollViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = ScrollView::class.java
}
