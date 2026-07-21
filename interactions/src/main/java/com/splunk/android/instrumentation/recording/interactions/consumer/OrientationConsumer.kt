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

package com.splunk.android.instrumentation.recording.interactions.consumer

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.OrientationEventListener
import android.view.Surface
import android.view.View
import androidx.annotation.RequiresApi
import com.splunk.rum.common.utils.extensions.activity
import com.splunk.android.instrumentation.recording.interactions.EventConsumer
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.extension.displayCompat
import com.splunk.android.instrumentation.recording.interactions.extension.isAssociatedWithDisplayCompat
import com.splunk.android.instrumentation.recording.interactions.extension.realSize
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.util.InteractionIdProvider

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
internal class OrientationConsumer(listener: OnInteractionListener) : EventConsumer(listener) {

    private val handler = Handler(Looper.getMainLooper())

    private var orientationListener: OrientationListener? = null
    private var isWideScreen = false

    private var pendingOrientation: Interaction.Orientation.Orientation? = null
    private var lastOrientation: Interaction.Orientation.Orientation? = null

    override fun onRootViewAdded(rootView: View) {
        val context = rootView.context

        if (!context.isAssociatedWithDisplayCompat)
            return

        if (pendingOrientation == null) {
            val orientation = rootView.activity?.resources?.configuration?.orientation?.toOrientation()

            if (orientation != null) {
                pendingOrientation = orientation
                reportOrientationChange.run()
            }
        }

        if (orientationListener != null)
            return

        val applicationContext = context.applicationContext
        val listener = OrientationListener(applicationContext)

        if (listener.canDetectOrientation()) {
            isWideScreen = isWideScreen(context)
            listener.enable()
        }

        applicationContext.registerComponentCallbacks(componentCallback)

        orientationListener = listener
    }

    private fun isWideScreen(context: Context): Boolean {
        val display = context.displayCompat
        val displaySize = display.realSize

        return when (display.rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 ->
                displaySize.x > displaySize.y
            Surface.ROTATION_90, Surface.ROTATION_270 ->
                displaySize.y > displaySize.x
            else ->
                false
        }
    }

    private fun Int.toOrientation(): Interaction.Orientation.Orientation {
        return when (this) {
            Configuration.ORIENTATION_PORTRAIT -> Interaction.Orientation.Orientation.PORTRAIT
            Configuration.ORIENTATION_LANDSCAPE -> Interaction.Orientation.Orientation.LANDSCAPE
            else -> Interaction.Orientation.Orientation.PORTRAIT
        }
    }

    private fun Interaction.Orientation.Orientation.swap(): Interaction.Orientation.Orientation {
        return if (this == Interaction.Orientation.Orientation.PORTRAIT)
            Interaction.Orientation.Orientation.LANDSCAPE
        else
            Interaction.Orientation.Orientation.PORTRAIT
    }

    private val reportOrientationChange = Runnable {
        val orientation = pendingOrientation ?: return@Runnable

        if (orientation == lastOrientation)
            return@Runnable

        lastOrientation = orientation

        val interaction = Interaction.Orientation(
            id = InteractionIdProvider.next(),
            timestamp = System.currentTimeMillis(),
            orientation = orientation
        )

        listener.onInteraction(interaction)
    }

    private val componentCallback = object : ComponentCallbacks {
        override fun onConfigurationChanged(newConfig: Configuration) {
            val orientation = newConfig.orientation.toOrientation()

            if (orientation == lastOrientation)
                return

            handler.removeCallbacks(reportOrientationChange)

            pendingOrientation = orientation
            reportOrientationChange.run()
        }

        override fun onLowMemory() {}
    }

    private inner class OrientationListener(context: Context) : OrientationEventListener(context) {

        override fun onOrientationChanged(angle: Int) {
            if (angle == ORIENTATION_UNKNOWN)
                return

            val orientation = convertAngleToOrientation(angle)

            if (orientation == pendingOrientation)
                return

            pendingOrientation = orientation

            handler.removeCallbacks(reportOrientationChange)
            handler.postDelayed(reportOrientationChange, ORIENTATION_CHANGE_DELAY)
        }

        private fun convertAngleToOrientation(angle: Int): Interaction.Orientation.Orientation {
            val rawOrientation = when (angle) {
                in 0..45, in 136..225, in 316..360 ->
                    Interaction.Orientation.Orientation.PORTRAIT
                in 46..135, in 226..315 ->
                    Interaction.Orientation.Orientation.LANDSCAPE
                else ->
                    Interaction.Orientation.Orientation.PORTRAIT
            }

            return if (isWideScreen)
                rawOrientation.swap()
            else
                rawOrientation
        }
    }

    private companion object {
        const val ORIENTATION_CHANGE_DELAY = 1000L
    }
}
