package com.splunk.android.instrumentation.recording.wireframe.descriptor

import android.widget.ViewAnimator

internal open class ViewAnimatorDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = ViewAnimator::class.java
}
