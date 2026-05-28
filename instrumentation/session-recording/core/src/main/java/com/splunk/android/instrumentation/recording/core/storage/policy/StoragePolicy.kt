package com.splunk.android.instrumentation.recording.core.storage.policy

import java.io.File
import kotlin.math.min

/**
 * [StoragePolicy] is used to define restrictions for a specific section (region) of stored data.
 */
internal data class StoragePolicy(
    val dir: File,
    val maxOccupiedSpace: Long,
    val maxOccupiedSpacePercentage: Float,
    val minStorageSpaceLeft: Long
) {
    fun check(freeSpace: Long): Boolean {
        val size = SizeCache.dirSize(dir)
        val maximalSize = min(maxOccupiedSpace, (maxOccupiedSpacePercentage * freeSpace).toLong())
        return size < maximalSize && freeSpace > minStorageSpaceLeft
    }
}
