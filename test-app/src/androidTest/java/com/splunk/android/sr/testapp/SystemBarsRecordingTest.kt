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

package com.splunk.android.sr.testapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.os.SystemClock
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.screenshot.model.Screenshot
import com.splunk.android.instrumentation.recording.screenshot.stats.ScreenshotStats
import com.splunk.android.sr.testapp.ui.dialog.DialogActivity
import com.splunk.android.sr.testapp.ui.systembars.SystemBarsActivity
import com.splunk.android.sr.testapp.ui.systembars.SystemBarsCase
import com.splunk.rum.common.utils.dpToPx
import java.io.File
import kotlin.math.abs
import org.junit.After
import org.junit.Assume
import org.junit.AssumptionViolatedException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compares the recorded frame with the real screen. Visible system bars, including the stripes behind them, have to be filled with the system bar
 * color exactly where the device shows them. Everything else has to be recorded as the user sees it.
 *
 * Pass `-e saveImages true` to store the compared images in the external files directory of the application.
 */
@RunWith(AndroidJUnit4::class)
class SystemBarsRecordingTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @Volatile
    private var recorded: Bitmap? = null

    @Volatile
    private var recordedAt = 0L

    private val listener = object : FrameCapturer.Listener {
        override fun onNewScreenshot(screenshot: Screenshot, stats: ScreenshotStats) {
            recorded = screenshot.bitmap.copy(Bitmap.Config.ARGB_8888, false)
            recordedAt = SystemClock.uptimeMillis()
        }
    }

    private var threadPolicy: StrictMode.ThreadPolicy? = null
    private var vmPolicy: StrictMode.VmPolicy? = null

    private val originalSecureSettings = LinkedHashMap<String, String>()

    @Before
    fun setUp() {
        Assume.assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

        putSecureSetting("immersive_mode_confirmations", "confirmed")
        vmPolicy = StrictMode.getVmPolicy()
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)

        instrumentation.runOnMainSync {
            threadPolicy = StrictMode.getThreadPolicy()
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
            FrameCapturer.listeners += listener
        }
    }

    @After
    fun tearDown() {
        instrumentation.runOnMainSync {
            FrameCapturer.listeners -= listener
            threadPolicy?.let(StrictMode::setThreadPolicy)
        }

        vmPolicy?.let(StrictMode::setVmPolicy)
        restoreSecureSettings()
    }

    @Test
    fun opaque() = verify(SystemBarsCase.OPAQUE)

    @Test
    fun edgeToEdge() = verify(SystemBarsCase.EDGE_TO_EDGE)

    @Test
    fun translucent() = verify(SystemBarsCase.TRANSLUCENT)

    @Test
    fun hiddenStatusBar() = verify(SystemBarsCase.HIDDEN_STATUS_BAR)

    @Test
    fun hiddenNavigationBar() = verify(SystemBarsCase.HIDDEN_NAVIGATION_BAR)

    @Test
    fun hiddenSystemBars() = verify(SystemBarsCase.HIDDEN_SYSTEM_BARS)

    @Test
    fun dialog() = verify(SystemBarsCase.DIALOG)

    @Test
    fun bottomSheet() = verify(SystemBarsCase.BOTTOM_SHEET)

    @Test
    fun keyboard() = verify(SystemBarsCase.KEYBOARD)

    @Test
    fun landscape() = verify(SystemBarsCase.LANDSCAPE)

    @Test
    fun dialogActivity() = verify(SystemBarsCase.DIALOG_ACTIVITY)

    private fun verify(case: SystemBarsCase) {
        val context = instrumentation.targetContext
        val referenceBars = if (case == SystemBarsCase.DIALOG_ACTIVITY) measureBars(SystemBarsActivity.createIntent(context, SystemBarsCase.EDGE_TO_EDGE)) else null

        if (case == SystemBarsCase.KEYBOARD)
            putSecureSetting("show_ime_with_hard_keyboard", "1")

        val intent = if (case == SystemBarsCase.DIALOG_ACTIVITY)
            Intent(context, DialogActivity::class.java)
        else
            SystemBarsActivity.createIntent(context, case)

        val scenario = ActivityScenario.launch<Activity>(intent)

        try {
            val keyboardTop = if (case == SystemBarsCase.KEYBOARD) awaitKeyboard(scenario) else null

            val recorded = awaitStableRecording() ?: throw AssertionError("[$case] Nothing was recorded.")
            val device = instrumentation.uiAutomation.takeScreenshot() ?: throw AssertionError("[$case] Screenshot of the device failed.")
            val bars = referenceBars ?: readBars(scenario)
            val stripes = readStripes(scenario)

            if (InstrumentationRegistry.getArguments().getString(ARGUMENT_SAVE_IMAGES) == "true")
                saveImages(case, device, recorded)

            if (recorded.width != device.width || recorded.height != device.height)
                throw AssertionError("[$case] Recording ${recorded.width}x${recorded.height} does not match the screen ${device.width}x${device.height}.")

            val reference = when {
                case == SystemBarsCase.DIALOG_ACTIVITY -> Reference.Solid(Color.BLACK) // Windows of other applications behind the dialog are not recorded
                keyboardTop != null -> Reference.Keyboard(device, ContextCompat.getColor(context, R.color.system_bars_content), keyboardTop)
                else -> Reference.Device(device)
            }

            Checker(case, recorded, reference, bars, stripes).check()
        } finally {
            scenario.close()
        }
    }

    private fun measureBars(intent: Intent): Insets {
        val scenario = ActivityScenario.launch<Activity>(intent)

        return try {
            SystemClock.sleep(MIN_WAIT_MS)
            readBars(scenario)
        } finally {
            scenario.close()
        }
    }

    private fun readBars(scenario: ActivityScenario<Activity>): Insets {
        var bars = Insets.NONE

        scenario.onActivity { activity ->
            val insets = ViewCompat.getRootWindowInsets(activity.window.decorView)
            if (insets != null)
                bars = Insets.max(insets.getInsets(WindowInsetsCompat.Type.statusBars()), insets.getInsets(WindowInsetsCompat.Type.navigationBars()))
        }

        return bars
    }

    /**
     * Returns screen positions of the vertical and the horizontal stripe or null when the screen has no stripes.
     */
    private fun readStripes(scenario: ActivityScenario<Activity>): Point? {
        var stripes: Point? = null

        scenario.onActivity { activity ->
            val vertical = activity.findViewById<View>(R.id.vertical_stripe) ?: return@onActivity
            val horizontal = activity.findViewById<View>(R.id.horizontal_stripe) ?: return@onActivity
            val location = IntArray(2)

            vertical.getLocationOnScreen(location)
            val x = location[0] + vertical.width / 2

            horizontal.getLocationOnScreen(location)
            val y = location[1] + horizontal.height / 2

            stripes = Point(x, y)
        }

        return stripes
    }

    /**
     * Returns the top of the keyboard on the screen.
     */
    private fun awaitKeyboard(scenario: ActivityScenario<Activity>): Int {
        val deadline = SystemClock.uptimeMillis() + MAX_WAIT_MS

        while (SystemClock.uptimeMillis() < deadline) {
            var keyboardTop: Int? = null

            scenario.onActivity { activity ->
                val decorView = activity.window.decorView
                val insets = ViewCompat.getRootWindowInsets(decorView) ?: return@onActivity
                val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

                if (keyboardHeight > insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
                    keyboardTop = decorView.height - keyboardHeight
            }

            keyboardTop?.let { return it }
            SystemClock.sleep(POLL_MS)
        }

        throw AssumptionViolatedException("On-screen keyboard is not available")
    }

    private fun awaitStableRecording(): Bitmap? {
        val start = SystemClock.uptimeMillis()
        recorded = null

        while (SystemClock.uptimeMillis() - start < MAX_WAIT_MS) {
            instrumentation.runOnMainSync { FrameCapturer.requestNewFrame() }
            SystemClock.sleep(POLL_MS)

            val now = SystemClock.uptimeMillis()
            if (now - start > MIN_WAIT_MS && recorded != null && now - recordedAt > QUIET_MS)
                break
        }

        return recorded
    }

    private fun putSecureSetting(name: String, value: String) {
        if (name !in originalSecureSettings)
            originalSecureSettings[name] = shell("settings get secure $name").trim()

        shell("settings put secure $name $value")
    }

    private fun restoreSecureSettings() {
        for ((name, value) in originalSecureSettings) {
            if (value.isEmpty() || value == "null")
                shell("settings delete secure $name")
            else
                shell("settings put secure $name $value")
        }

        originalSecureSettings.clear()
    }

    /**
     * Returns the output of the command. Reading the output until the end waits for the command to finish.
     */
    private fun shell(command: String): String {
        val descriptor = instrumentation.uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText() }
    }

    private fun saveImages(case: SystemBarsCase, device: Bitmap, recorded: Bitmap) {
        val dir = File(instrumentation.targetContext.getExternalFilesDir(null), "system_bars")
        dir.mkdirs()

        val prefix = "${Build.VERSION.SDK_INT}_${case.name}"
        File(dir, "${prefix}_device.png").outputStream().use { device.compress(Bitmap.CompressFormat.PNG, 100, it) }
        File(dir, "${prefix}_recording.png").outputStream().use { recorded.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /**
     * Color which the recording has to contain at the given point outside of the system bars.
     */
    private sealed class Reference {

        abstract fun colorAt(x: Int, y: Int): Int

        class Device(private val device: Bitmap) : Reference() {
            override fun colorAt(x: Int, y: Int) = device.getPixel(x, y)
        }

        class Solid(private val color: Int) : Reference() {
            override fun colorAt(x: Int, y: Int) = color
        }

        /**
         * The keyboard covers the screen below [keyboardTop] on the device, the recording shows the application there.
         */
        class Keyboard(private val device: Bitmap, private val contentColor: Int, private val keyboardTop: Int) : Reference() {
            override fun colorAt(x: Int, y: Int) = if (y >= keyboardTop) contentColor else device.getPixel(x, y)
        }
    }

    private class Checker(
        private val case: SystemBarsCase,
        private val recorded: Bitmap,
        private val reference: Reference,
        private val bars: Insets,
        private val stripes: Point?
    ) {

        private val width = recorded.width
        private val height = recorded.height
        private val gap = dpToPx(OUTSIDE_GAP_DP)
        private val failures = ArrayList<String>()

        fun check() {
            val horizontalSample = (width * HORIZONTAL_RATIO).toInt()
            val verticalSample = (height * VERTICAL_RATIO).toInt()

            checkSide("status bar", bars.top, width / 2, stripes?.x) { depth, along -> Pair(along, depth) }
            checkSide("bottom navigation bar", bars.bottom, horizontalSample, stripes?.x) { depth, along -> Pair(along, height - 1 - depth) }
            checkSide("left navigation bar", bars.left, verticalSample, stripes?.y) { depth, along -> Pair(depth, along) }
            checkSide("right navigation bar", bars.right, verticalSample, stripes?.y) { depth, along -> Pair(width - 1 - depth, along) }

            if (failures.isNotEmpty())
                throw AssertionError("[$case] bars=$bars\n" + failures.joinToString("\n"))
        }

        /**
         * @param sample position along the bar which avoids icons, navigation buttons and the debugger bubble.
         * @param stripe position of the stripe along the bar.
         */
        private fun checkSide(name: String, size: Int, sample: Int, stripe: Int?, point: (depth: Int, along: Int) -> Pair<Int, Int>) {
            if (size > 0) {
                val outside = point(size + gap, sample)

                expect("outside of the $name", outside, reference.colorAt(outside.first, outside.second))
                expect("inside of the $name", point(size / 2, sample), SYSTEM_BAR_COLOR)

                if (stripe != null)
                    expect("stripe below the $name", point(size / 2, stripe), SYSTEM_BAR_COLOR)
            } else {
                val edge = point(EDGE_DEPTH_PX, sample)
                expect("hidden $name", edge, reference.colorAt(edge.first, edge.second))

                if (stripe != null) {
                    val stripeEdge = point(EDGE_DEPTH_PX, stripe)
                    expect("stripe behind the hidden $name", stripeEdge, reference.colorAt(stripeEdge.first, stripeEdge.second))
                }
            }
        }

        private fun expect(description: String, point: Pair<Int, Int>, expected: Int) {
            val actual = recorded.getPixel(point.first, point.second)

            if (!isSimilar(actual, expected))
                failures += "$description at $point: recorded ${actual.hex()}, expected ${expected.hex()}"
        }

        private fun isSimilar(a: Int, b: Int): Boolean {
            return abs(Color.red(a) - Color.red(b)) <= COLOR_TOLERANCE &&
                abs(Color.green(a) - Color.green(b)) <= COLOR_TOLERANCE &&
                abs(Color.blue(a) - Color.blue(b)) <= COLOR_TOLERANCE
        }

        private fun Int.hex() = String.format("#%08X", this)
    }

    private companion object {
        const val ARGUMENT_SAVE_IMAGES = "saveImages"

        const val MIN_WAIT_MS = 3_000L
        const val MAX_WAIT_MS = 12_000L
        const val QUIET_MS = 1_200L
        const val POLL_MS = 250L

        const val SYSTEM_BAR_COLOR = 0xFF7C8697.toInt()
        const val COLOR_TOLERANCE = 16
        const val OUTSIDE_GAP_DP = 12f
        const val EDGE_DEPTH_PX = 4

        const val HORIZONTAL_RATIO = 0.06
        const val VERTICAL_RATIO = 0.12
    }
}
