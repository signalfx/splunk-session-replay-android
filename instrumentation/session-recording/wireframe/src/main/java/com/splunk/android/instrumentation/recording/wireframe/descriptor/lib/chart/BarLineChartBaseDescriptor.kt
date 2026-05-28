package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart

import com.splunk.android.common.utils.extensions.toClass

internal open class BarLineChartBaseDescriptor : ChartDescriptor() {

    override val intendedClass: Class<*>? = "com.github.mikephil.charting.charts.BarLineChartBase".toClass()
}
