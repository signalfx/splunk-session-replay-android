package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.vico

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor

/* MARK
 *  - Compatible with com.patrykandpatrick.vico:core:2.0.0-beta.1, com.patrykandpatrick.vico:views:2.0.0-beta.1
 */
internal open class ChartViewDescriptor : FrameLayoutDescriptor() {

    override val intendedClass: Class<*>? = "com.patrykandpatrick.vico.views.common.ChartView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
