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

import android.view.KeyEvent
import android.view.View
import com.splunk.android.instrumentation.recording.interactions.EventConsumer
import com.splunk.android.instrumentation.recording.interactions.OnInteractionListener
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.Interaction.PhoneButton.Name
import com.splunk.android.instrumentation.recording.interactions.util.InteractionIdProvider

internal class ButtonConsumer(listener: OnInteractionListener) : EventConsumer(listener) {

    override fun onKeyEvent(rootView: View, event: KeyEvent) {
        if (event.action != KeyEvent.ACTION_UP)
            return

        val id = InteractionIdProvider.next()
        val timestamp = System.currentTimeMillis()

        val button = when (event.keyCode) {
            KeyEvent.KEYCODE_BACK ->
                Interaction.PhoneButton(id, timestamp, Name.BACK)
            KeyEvent.KEYCODE_VOLUME_DOWN ->
                Interaction.PhoneButton(id, timestamp, Name.VOLUME_DOWN)
            KeyEvent.KEYCODE_VOLUME_UP ->
                Interaction.PhoneButton(id, timestamp, Name.VOLUME_UP)
            else ->
                return
        }

        listener.onInteraction(button)
    }
}
