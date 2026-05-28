package com.splunk.android.debugger.view.stats

import android.content.Context
import android.util.AttributeSet
import com.splunk.android.debugger.R
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

internal class WireframeStackedChartView(context: Context, attrs: AttributeSet? = null) : StackedChartView<WireframeStats>(context, attrs) {

    private val otherBarColor = getColor(R.color.sld_wireframe_stats_others)
    private val textBarColor = getColor(R.color.sld_wireframe_stats_text)
    private val generalDrawableBarColor = getColor(R.color.sld_wireframe_stats_general_drawable)
    private val canvasColor = getColor(R.color.sld_wireframe_stats_canvas)

    override fun draw(item: WireframeStats, drawer: (fraction: Float, color: Int) -> Unit) {
        drawer(item.run { canvasTime / totalTime }, canvasColor)
        drawer(item.run { generalDrawablesTime / totalTime }, generalDrawableBarColor)
        drawer(item.run { textsTime / totalTime }, textBarColor)
        drawer(item.run { othersTime / totalTime }, otherBarColor)
    }

    override fun getTotalValue(item: WireframeStats): Float {
        return item.totalTime
    }
}
