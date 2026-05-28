package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart

import com.splunk.android.common.utils.extensions.toClass

internal open class BarChartDescriptor : BarLineChartBaseDescriptor() {

    override val intendedClass: Class<*>? = "com.github.mikephil.charting.charts.BarChart".toClass()
}
