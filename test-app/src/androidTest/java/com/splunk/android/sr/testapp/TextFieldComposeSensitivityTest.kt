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

import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.api.SessionReplay
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.sr.testapp.ui.compose.LABEL_MULTI_LINE_FIXED_HEIGHT
import com.splunk.android.sr.testapp.ui.compose.LABEL_MULTI_LINE_WRAP_HEIGHT
import com.splunk.android.sr.testapp.ui.compose.LABEL_NOT_SENSITIVE_ANCESTOR
import com.splunk.android.sr.testapp.ui.compose.LABEL_NOT_SENSITIVE_TEXT_FIELD
import com.splunk.android.sr.testapp.ui.compose.LABEL_SINGLE_LINE
import com.splunk.android.sr.testapp.ui.compose.PLACEHOLDER
import com.splunk.android.sr.testapp.ui.compose.TextFieldComposeActivity
import com.splunk.android.sr.testapp.ui.compose.VALUE_IN_NOT_SENSITIVE_ANCESTOR
import com.splunk.android.sr.testapp.ui.compose.VALUE_IN_NOT_SENSITIVE_TEXT_FIELD
import org.junit.After
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextFieldComposeSensitivityTest {

    private var scenario: ActivityScenario<TextFieldComposeActivity>? = null

    @Before
    fun setUp() {
        Assume.assumeTrue(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)

        FrameCapturer.frameHolder.clearWireframeFrames()
    }

    @After
    fun tearDown() {
        SessionReplay.instance.sensitivity.compose.textFieldSensitivity = true
        scenario?.close()
        scenario = null
    }

    @Test
    fun textFieldsAreSensitiveByDefault() {
        SessionReplay.instance.sensitivity.compose.textFieldSensitivity = true

        val wireframe = recordScreen()

        wireframe.assertContainsTexts(
            LABEL_SINGLE_LINE,
            LABEL_MULTI_LINE_FIXED_HEIGHT,
            LABEL_MULTI_LINE_WRAP_HEIGHT,
            LABEL_NOT_SENSITIVE_ANCESTOR,
            LABEL_NOT_SENSITIVE_TEXT_FIELD
        )

        wireframe.assertDoesNotContainTexts(
            PLACEHOLDER,
            VALUE_IN_NOT_SENSITIVE_ANCESTOR
        )

        wireframe.assertContainsTexts(VALUE_IN_NOT_SENSITIVE_TEXT_FIELD)

        if (wireframe.sensitiveViewCount < EXPECTED_SENSITIVE_TEXT_FIELD_COUNT)
            throw AssertionError("Expected at least $EXPECTED_SENSITIVE_TEXT_FIELD_COUNT sensitive views, found ${wireframe.sensitiveViewCount}.")
    }

    @Test
    fun textFieldsAreNotSensitiveWhenApiDisablesIt() {
        SessionReplay.instance.sensitivity.compose.textFieldSensitivity = false

        val wireframe = recordScreen()

        wireframe.assertContainsTexts(
            LABEL_SINGLE_LINE,
            PLACEHOLDER,
            VALUE_IN_NOT_SENSITIVE_ANCESTOR,
            VALUE_IN_NOT_SENSITIVE_TEXT_FIELD
        )
    }

    private fun recordScreen(): RecordedWireframe {
        scenario = ActivityScenario.launch(TextFieldComposeActivity::class.java)

        awaitText(LABEL_SINGLE_LINE)
        scrollUntilRecorded(VALUE_IN_NOT_SENSITIVE_TEXT_FIELD)

        return collectRecordedWireframe()
    }

    private fun awaitText(text: String) {
        val deadline = SystemClock.uptimeMillis() + AWAIT_TIMEOUT_MS

        while (SystemClock.uptimeMillis() < deadline) {
            if (text in collectRecordedWireframe().texts)
                return

            requestNewFrame()
            SystemClock.sleep(AWAIT_POLL_MS)
        }

        val frameCount = FrameCapturer.frameHolder.getWireframeFramesCopy().size
        val recorded = collectRecordedWireframe()

        throw AssertionError(
            "Text '$text' was not recorded within $AWAIT_TIMEOUT_MS ms. " +
                "Frames: $frameCount, sensitive views: ${recorded.sensitiveViewCount}, texts: ${recorded.texts.sorted()}"
        )
    }

    private fun requestNewFrame() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { FrameCapturer.requestNewFrame() }
    }

    private fun scrollUntilRecorded(text: String) {
        repeat(SWIPE_ATTEMPTS) {
            if (text in collectRecordedWireframe().texts)
                return

            swipeUp()
            requestNewFrame()
            SystemClock.sleep(SWIPE_SETTLE_MS)
        }

        awaitText(text)
    }

    private fun swipeUp() {
        var width = 0
        var height = 0

        scenario?.onActivity {
            width = it.window.decorView.width
            height = it.window.decorView.height
        }

        if (width == 0 || height == 0)
            return

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val x = width / 2f
        val from = height * 0.8f
        val to = height * 0.2f
        val downTime = SystemClock.uptimeMillis()

        val send = { action: Int, y: Float ->
            val event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0)
            instrumentation.sendPointerSync(event)
            event.recycle()
        }

        send(MotionEvent.ACTION_DOWN, from)

        for (step in 1..SWIPE_STEPS) {
            send(MotionEvent.ACTION_MOVE, from + (to - from) * step / SWIPE_STEPS)
            SystemClock.sleep(SWIPE_STEP_MS)
        }

        send(MotionEvent.ACTION_UP, to)
    }

    private fun collectRecordedWireframe(): RecordedWireframe {
        val result = RecordedWireframe()

        for (frame in FrameCapturer.frameHolder.getWireframeFramesCopy())
            for (scene in frame.scenes)
                for (window in scene.windows) {
                    window.skeletons?.let(result::collectTexts)
                    window.subviews?.forEach(result::collect)
                }

        return result
    }

    private class RecordedWireframe {

        val texts = HashSet<String>()

        var sensitiveViewCount = 0
            private set

        fun collect(view: Wireframe.Frame.Scene.Window.View) {
            if (view.isSensitive == true)
                sensitiveViewCount++

            view.skeletons?.let(::collectTexts)
            view.foregroundSkeletons?.let(::collectTexts)
            view.subviews?.forEach(::collect)
        }

        fun collectTexts(skeletons: List<Wireframe.Frame.Scene.Window.View.Skeleton>) {
            for (skeleton in skeletons)
                if (skeleton is Wireframe.Frame.Scene.Window.View.Skeleton.Text)
                    texts += skeleton.text.toString()
        }

        fun assertContainsTexts(vararg expected: String) {
            for (text in expected)
                if (text !in texts)
                    throw AssertionError("Text '$text' is missing in the wireframe. Recorded texts: ${texts.sorted()}")
        }

        fun assertDoesNotContainTexts(vararg unexpected: String) {
            for (text in unexpected)
                if (text in texts)
                    throw AssertionError("Text '$text' leaked into the wireframe, it should have been covered.")
        }
    }

    private companion object {
        const val AWAIT_TIMEOUT_MS = 20_000L
        const val AWAIT_POLL_MS = 200L

        const val SWIPE_ATTEMPTS = 4
        const val SWIPE_STEPS = 10
        const val SWIPE_STEP_MS = 16L
        const val SWIPE_SETTLE_MS = 1_500L

        const val EXPECTED_SENSITIVE_TEXT_FIELD_COUNT = 4
    }
}
