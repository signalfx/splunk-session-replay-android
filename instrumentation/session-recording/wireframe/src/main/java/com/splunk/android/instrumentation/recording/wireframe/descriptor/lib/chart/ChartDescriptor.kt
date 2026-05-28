package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor

/* MARK
 *  - Compatible with com.github.PhilJay:MPAndroidChart:v3.0.3
 */
internal open class ChartDescriptor : ViewGroupDescriptor() {

    override val intendedClass: Class<*>? = "com.github.mikephil.charting.charts.BarChart".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
