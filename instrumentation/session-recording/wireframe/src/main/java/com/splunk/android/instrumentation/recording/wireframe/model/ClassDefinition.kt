package com.splunk.android.instrumentation.recording.wireframe.model

import android.os.Parcelable
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import kotlinx.parcelize.Parcelize

/**
 * Class definition.
 *
 * @param isInternal Whether the class is probably internal implementation. Means, this probably is not public library.
 *
 * @see WireframeExtractor
 */
@Parcelize
data class ClassDefinition internal constructor(
    val className: String,
    val ancestors: List<String>,
    val isInternal: Boolean
) : Parcelable
