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

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Records the classes and methods used while the app starts up and renders its first screen.
 *
 * The result is written to the device and pulled by Gradle into
 * `macrobenchmark/build/outputs/managed_device_android_test_additional_output` (or reported in the
 * test output); copy it to `test-app/src/main/baseline-prof.txt`.
 *
 * Requires an API 28+ device that is either rooted or running a userdebug/AOSP build.
 */
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun startup() = rule.collect(
        packageName = TargetApp.PACKAGE_NAME,
        includeInStartupProfile = false // Startup profiles are only consumed by AGP 8.x dex layout optimizations, so with AGP 7.3 we only want the plain baseline profile.
    ) {
        pressHome()
        startActivityAndWait()

        device.wait(Until.hasObject(By.pkg(TargetApp.PACKAGE_NAME).depth(0)), 5_000)
        device.waitForIdle()

        device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4, device.displayWidth / 2, device.displayHeight / 4, 10)
        device.waitForIdle()
    }
}
