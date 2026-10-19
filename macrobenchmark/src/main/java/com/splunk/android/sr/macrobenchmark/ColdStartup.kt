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

package com.splunk.android.sr.macrobenchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule

internal fun MacrobenchmarkRule.measureColdStartup(compilationMode: CompilationMode) = measureRepeated(
    packageName = TargetApp.PACKAGE_NAME,
    metrics = listOf(StartupTimingMetric()),
    compilationMode = compilationMode,
    startupMode = StartupMode.COLD,
    iterations = TargetApp.STARTUP_ITERATIONS
) {
    pressHome()
    startActivityAndWait { intent ->
        intent.putExtra(TargetApp.EXTRA_SKIP_SPLASH_SCREEN_HOLD, true)
    }
}
