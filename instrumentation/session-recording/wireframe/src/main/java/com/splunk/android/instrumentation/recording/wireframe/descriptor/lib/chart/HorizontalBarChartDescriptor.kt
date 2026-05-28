package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart

import com.splunk.android.common.utils.extensions.toClass

internal open class HorizontalBarChartDescriptor : BarChartDescriptor() {

    override val intendedClass: Class<*>? = "com.github.mikephil.charting.charts.HorizontalBarChart".toClass()
}
