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

package com.splunk.android.instrumentation.recording.wireframe.stats

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal object StatsCollector {

    private var frameTime = 0L
    private var windowCount = 0
    private var drawablesTime = 0L
    private var drawablesCount = 0
    private var textsTime = 0L
    private var textsCount = 0
    private var canvasTime = 0f
    private var canvasCount = 0
    private var canvasSkeletonsCount = 0

    @OptIn(ExperimentalContracts::class)
    inline fun measureWindow(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        windowCount++
        measure(block) { frameTime += it }
    }

    inline fun measureDrawableTime(block: () -> Unit) {
        drawablesCount++
        measure(block) { drawablesTime += it }
    }

    inline fun measureTextsTime(block: () -> Unit) {
        textsCount++
        measure(block) { textsTime += it }
    }

    inline fun measureCanvasTime(block: () -> Int) {
        canvasCount++
        canvasSkeletonsCount += measure(block) { canvasTime += it }
    }

    fun popWireframeStats(): WireframeStats {
        val stats = WireframeStats(
            totalTime = frameTime / 1000_000f,
            windowCount = windowCount,
            generalDrawablesTime = drawablesTime / 1000_000f,
            generalDrawablesCount = drawablesCount,
            textsTime = textsTime / 1000_000f,
            textsCount = textsCount,
            canvasTime = canvasTime / 1000_000f,
            canvasCount = canvasCount,
            canvasSkeletonsCount = canvasSkeletonsCount
        )

        frameTime = 0L
        windowCount = 0
        drawablesTime = 0L
        drawablesCount = 0
        textsTime = 0L
        textsCount = 0
        canvasTime = 0f
        canvasCount = 0
        canvasSkeletonsCount = 0

        return stats
    }

    private inline fun <R> measure(block: () -> R, result: (Long) -> Unit): R {
        val timestamp = System.nanoTime()

        try {
            return block()
        } finally {
            result(System.nanoTime() - timestamp)
        }
    }
}
