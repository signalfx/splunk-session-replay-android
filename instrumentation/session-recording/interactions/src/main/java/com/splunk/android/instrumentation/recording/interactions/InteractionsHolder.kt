package com.splunk.android.instrumentation.recording.interactions

import com.splunk.android.instrumentation.recording.interactions.extension.insertOrdered
import com.splunk.android.instrumentation.recording.interactions.model.Interaction
import com.splunk.android.instrumentation.recording.interactions.model.LegacyData
import kotlin.math.sign

class InteractionsHolder internal constructor() {

    private val interactions = ArrayList<Interaction>(INITIAL_CAPACITY)
    private val legacyDataMap = HashMap<Int, LegacyData>(INITIAL_CAPACITY)

    var timeLimit: Long = DEFAULT_TIME_LIMIT

    @Synchronized
    fun getLegacyData(interaction: Interaction): LegacyData? {
        return legacyDataMap[interaction.id]
    }

    @Synchronized
    fun getInteractions(): List<Interaction> {
        limitHistory()
        return interactions
    }

    @Synchronized
    fun getInteractionsCopy(): List<Interaction> {
        return getInteractions().toList()
    }

    @Synchronized
    fun clear() {
        interactions.clear()
        legacyDataMap.clear()
    }

    @Synchronized
    internal fun storeInteraction(interaction: Interaction, legacyData: LegacyData?) {
        interactions.insertOrdered(interaction, true) { o1, o2 -> (o1.timestamp - o2.timestamp).sign }

        if (legacyData != null)
            legacyDataMap[interaction.id] = legacyData

        limitHistory()
    }

    private fun limitHistory() { // TODO Investigate better solution
        val timestamp = System.currentTimeMillis()
        val iterator = interactions.iterator()

        while (iterator.hasNext()) {
            val interaction = iterator.next()

            if (timestamp - interaction.timestamp > timeLimit) {
                legacyDataMap.remove(interaction.id)
                iterator.remove()
            } else
                break
        }
    }

    private companion object {
        const val INITIAL_CAPACITY = 500
        const val DEFAULT_TIME_LIMIT = 3 * 60 * 1000L
    }
}
