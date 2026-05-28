package com.splunk.android.instrumentation.recording.interactions

import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData

interface OnInteractionListener {
    fun onInteraction(interaction: Interaction, legacyData: LegacyData? = null)
}
