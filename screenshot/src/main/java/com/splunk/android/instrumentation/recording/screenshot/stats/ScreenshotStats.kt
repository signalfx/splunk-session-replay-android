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

class ScreenshotStats internal constructor(
    val totalTime: Float,
    val copyTime: Float,
    val windowCopyTime: Float,
    val surfaceCopyTime: Float,
    val finalDrawTime: Float,
    val windowCount: Int,
    val surfaceCount: Int,
    val sensitivityTime: Float
) {

    val othersTime: Float = totalTime - copyTime - sensitivityTime - finalDrawTime

    override fun toString(): String {
        return "ScreenshotStats(totalTime=$totalTime, copyTime=$copyTime, windowCopyTime=$windowCopyTime, surfaceCopyTime=$surfaceCopyTime, finalDrawTime=$finalDrawTime, windowCount=$windowCount, surfaceCount=$surfaceCount, sensitivityTime=$sensitivityTime, othersTime=$othersTime)"
    }

    companion object
}
