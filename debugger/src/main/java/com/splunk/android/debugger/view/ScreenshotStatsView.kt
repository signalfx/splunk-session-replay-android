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

package com.splunk.android.debugger.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.splunk.android.debugger.R
import com.splunk.android.debugger.databinding.SldViewScreenshotStatsBinding
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats

internal class ScreenshotStatsView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val viewBinding = SldViewScreenshotStatsBinding.inflate(LayoutInflater.from(context), this, true)

    fun addStats(stats: ScreenshotStats) {
        viewBinding.stats.addItem(stats)
        update()
    }

    fun getStatsList(): List<ScreenshotStats> {
        return viewBinding.stats.getItems()
    }

    fun setStatsList(list: List<ScreenshotStats>) {
        viewBinding.stats.setItems(list)
        update()
    }

    private fun update() {
        val stats = viewBinding.stats.getItems().firstOrNull() ?: return
        val maxTotalTime = viewBinding.stats.getItems().maxByOrNull { it.totalTime }?.totalTime ?: 0f

        viewBinding.maxTotalTime.text = context.getString(R.string.sld_screenshot_stats_max_total_time, maxTotalTime)
        viewBinding.lastTotalTime.text = context.getString(R.string.sld_screenshot_stats_last_total_time, stats.totalTime)
        viewBinding.lastOthersTime.text = context.getString(R.string.sld_screenshot_stats_last_other_time, stats.othersTime)
        viewBinding.lastFinalDrawTime.text = context.getString(R.string.sld_screenshot_stats_last_final_draw_time, stats.finalDrawTime)
        viewBinding.lastSensitivityTime.text = context.getString(R.string.sld_screenshot_stats_last_sensitivity_time, stats.sensitivityTime)
        viewBinding.lastCopyTime.text = context.getString(R.string.sld_screenshot_stats_last_copy_time, stats.copyTime)
        viewBinding.lastSurfaceCopyTime.text = context.getString(R.string.sld_screenshot_stats_last_copy_surface_time, stats.surfaceCopyTime, stats.surfaceCount)
        viewBinding.lastWindowCopyTime.text = context.getString(R.string.sld_screenshot_stats_last_copy_window_time, stats.windowCopyTime, stats.windowCount)
    }
}
