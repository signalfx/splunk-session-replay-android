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
