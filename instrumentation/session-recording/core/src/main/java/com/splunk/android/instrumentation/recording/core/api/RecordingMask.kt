package com.splunk.android.instrumentation.recording.core.api

import android.graphics.Rect

/**
 * Masks rectangle on the screen by defined behaviour.
 *
 * @see SessionReplay.recordingMask
 */
data class RecordingMask(val elements: List<Element>) {

    /**
     * Mask element.
     *
     * @param rect Screen space rectangle
     * @param type Mask type. Default is [Type.COVERING].
     */
    data class Element @JvmOverloads constructor(val rect: Rect, val type: Type = Type.COVERING) {

        /**
         * Element mask type.
         */
        enum class Type {

            /**
             * Cover rectangle on the screen.
             */
            COVERING,

            /**
             * Uncover rectangle on the screen. For example, when one [Element] covers the whole screen and another uncover center of the screen, the result will be uncovered "hole".
             */
            ERASING
        }
    }
}
