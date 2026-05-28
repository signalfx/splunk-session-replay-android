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
import com.splunk.android.debugger.databinding.SldViewWireframeStatsBinding
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

internal class WireframeStatsView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val viewBinding = SldViewWireframeStatsBinding.inflate(LayoutInflater.from(context), this, true)

    fun addStats(stats: WireframeStats) {
        viewBinding.stats.addItem(stats)
        update()
    }

    fun getStatsList(): List<WireframeStats> {
        return viewBinding.stats.getItems()
    }

    fun setStatsList(list: List<WireframeStats>) {
        viewBinding.stats.setItems(list)
        update()
    }

    private fun update() {
        val stats = viewBinding.stats.getItems().firstOrNull() ?: return
        val maxTotalTime = viewBinding.stats.getItems().maxByOrNull { it.totalTime }?.totalTime ?: 0f

        viewBinding.maxTotalTime.text = context.getString(R.string.sld_wireframe_stats_max_total_time, maxTotalTime)
        viewBinding.lastTotalTime.text = context.getString(R.string.sld_wireframe_stats_last_total_time, stats.totalTime, stats.windowCount)
        viewBinding.lastOthersTime.text = context.getString(R.string.sld_wireframe_stats_last_other_time, stats.othersTime)
        viewBinding.lastTextTime.text = context.getString(R.string.sld_wireframe_stats_last_text_time, stats.textsTime, stats.textsCount)
        viewBinding.lastDrawableTime.text = context.getString(R.string.sld_wireframe_stats_last_drawable_time, stats.generalDrawablesTime, stats.generalDrawablesCount)
        viewBinding.lastCanvasTime.text = context.getString(R.string.sld_wireframe_stats_last_canvas_time, stats.canvasTime, stats.canvasCount, stats.canvasSkeletonsCount)
    }
}
