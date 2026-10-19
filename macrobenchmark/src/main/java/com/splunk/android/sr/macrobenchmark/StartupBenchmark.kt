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

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/**
 * Cold start of the app measured under three compilation modes.
 *
 * - [noCompilation] is the "before" number: nothing is AOT compiled, everything is interpreted or
 *   JIT compiled on the fly. This is what a user gets right after installing an app without a
 *   baseline profile.
 * - [baselineProfile] is the "after" number: only the classes and methods listed in the baseline
 *   profile are AOT compiled.
 * - [fullCompilation] is the theoretical best case and serves as a reference point for how much of
 *   the possible gain the baseline profile actually captures.
 */
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun noCompilation() = rule.measureColdStartup(CompilationMode.None())

    @Test
    fun baselineProfile() = rule.measureColdStartup(
        CompilationMode.Partial(baselineProfileMode = BaselineProfileMode.Require)
    )

    @Test
    fun fullCompilation() = rule.measureColdStartup(CompilationMode.Full())
}
