package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RatingBarDescriptor

internal open class AppCompatRatingBarDescriptor : RatingBarDescriptor() {

    override val intendedClass: Class<*>? = "androidx.appcompat.widget.AppCompatRatingBar".toClass()
}
