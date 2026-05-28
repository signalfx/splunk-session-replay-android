/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
