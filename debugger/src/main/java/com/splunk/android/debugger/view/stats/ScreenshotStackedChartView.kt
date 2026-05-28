package com.splunk.android.debugger.view.stats

import android.content.Context
import android.util.AttributeSet
import com.splunk.android.debugger.R
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats

internal class ScreenshotStackedChartView(context: Context, attrs: AttributeSet? = null) : StackedChartView<ScreenshotStats>(context, attrs) {

    private val otherBarColor = getColor(R.color.sld_screenshot_render_stats_others)
    private val finalDrawColor = getColor(R.color.sld_screenshot_render_stats_final_draw)
    private val sensitivityBarColor = getColor(R.color.sld_screenshot_render_stats_sensitivity)
    private val copyColor = getColor(R.color.sld_screenshot_render_stats_copy)

    override fun draw(item: ScreenshotStats, drawer: (fraction: Float, color: Int) -> Unit) {
        drawer(item.run { copyTime / totalTime }, copyColor)
        drawer(item.run { sensitivityTime / totalTime }, sensitivityBarColor)
        drawer(item.run { finalDrawTime / totalTime }, finalDrawColor)
        drawer(item.run { othersTime / totalTime }, otherBarColor)
    }

    override fun getTotalValue(item: ScreenshotStats): Float {
        return item.totalTime
    }
}
