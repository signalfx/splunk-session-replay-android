package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.preference

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageViewDescriptor

internal open class PreferenceImageViewDescriptor : AppCompatImageViewDescriptor() {

    override val intendedClass: Class<*>? = "androidx.preference.internal.PreferenceImageView".toClass()
}
