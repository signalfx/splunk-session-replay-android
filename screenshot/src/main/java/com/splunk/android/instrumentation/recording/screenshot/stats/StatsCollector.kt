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

package com.splunk.android.instrumentation.recording.screenshot.stats

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal object StatsCollector {

    private var totalTime = 0L
    private var copyTime = 0L
    private var windowCopyTime = 0L
    private var surfaceCopyTime = 0L
    private var finalDrawTime = 0L
    private var windowCount = 0
    private var surfaceCount = 0
    private var sensitivityTime = 0L

    @OptIn(ExperimentalContracts::class)
    inline fun measureGeneralTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        measure(block) { totalTime += it }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun measureCopyTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        measure(block) { copyTime += it }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun measureWindowCopyTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        windowCount++
        measure(block) { windowCopyTime += it }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun measureSurfaceCopyTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        surfaceCount++
        measure(block) { surfaceCopyTime += it }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun measureFinalDrawTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        measure(block) { finalDrawTime += it }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun measureSensitivityTime(block: () -> Unit) {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        measure(block) { sensitivityTime += it }
    }

    fun popStats(): ScreenshotStats {
        val stats = ScreenshotStats(
            totalTime = totalTime / 1000_000f,
            copyTime = copyTime / 1000_000f,
            windowCopyTime = windowCopyTime / 1000_000f,
            surfaceCopyTime = surfaceCopyTime / 1000_000f,
            finalDrawTime = finalDrawTime / 1000_000f,
            windowCount = windowCount,
            surfaceCount = surfaceCount,
            sensitivityTime = sensitivityTime / 1000_000f
        )

        totalTime = 0L
        copyTime = 0L
        windowCopyTime = 0L
        surfaceCopyTime = 0L
        finalDrawTime = 0L
        windowCount = 0
        surfaceCount = 0
        sensitivityTime = 0L

        return stats
    }

    @OptIn(ExperimentalContracts::class)
    private inline fun <R> measure(block: () -> R, crossinline result: (Long) -> Unit): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

        val timestamp = System.nanoTime()

        try {
            return block()
        } finally {
            result(System.nanoTime() - timestamp)
        }
    }
}
