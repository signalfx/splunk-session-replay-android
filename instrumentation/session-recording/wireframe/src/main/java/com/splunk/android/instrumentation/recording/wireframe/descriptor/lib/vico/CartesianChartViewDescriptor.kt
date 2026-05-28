package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.vico

import android.view.View
import com.splunk.android.common.utils.extensions.toClass

internal open class CartesianChartViewDescriptor : ChartViewDescriptor() {

    override val intendedClass: Class<*>? = "com.patrykandpatrick.vico.views.cartesian.CartesianChartView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.CANVAS
    }
}
