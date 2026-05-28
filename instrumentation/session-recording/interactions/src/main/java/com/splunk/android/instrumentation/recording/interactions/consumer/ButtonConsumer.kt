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
