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

package com.splunk.android.instrumentation.recording.core.api

import android.view.View
import android.widget.EditText
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler

/**
 * Defines which part of screen will not be visible.
 *
 * @see SessionReplay.sensitivity
 */
class Sensitivity internal constructor(
    private val api: SensitivityApiHandler
) {

    /**
     * Sets sensitivity for the [view] instance. Whether to cover the [view] in the data chunk or null to not change behaviour by the [view] instance.
     */
    fun <T : View> setViewInstanceSensitivity(view: T, isSensitive: Boolean?) {
        api.setViewInstanceSensitivity(view, isSensitive)
    }

    /**
     * Sets [View] class sensitivity. Whether to cover all instances of [clazz] in the data chunk or null to not change the behaviour by [clazz].
     * By default, [EditText] is sensitive. Class sensitivity can be overridden by instance sensitivity.
     *
     * @see setViewInstanceSensitivity
     */
    fun <T : View> setViewClassSensitivity(clazz: Class<T>, isSensitive: Boolean?) {
        api.setViewClassSensitivity(clazz, isSensitive)
    }

    /**
     * Returns sensitivity of the [view] instance.
     *
     * @return Whether the [view] will be covered in the data chunk or null when the behaviour is not defined by the [view] instance.
     * @see setViewInstanceSensitivity
     */
    fun <T : View> getViewInstanceSensitivity(view: T): Boolean? = api.getViewInstanceSensitivity(view)

    /**
     * Returns View class sensitivity. By default, [EditText] is sensitive. Class sensitivity can be overridden by instance sensitivity.
     *
     * @return Whether all instances of [clazz] will be covered in the data chunk or null when the behaviour is not defined by [clazz].
     */
    fun <T : View> getViewClassSensitivity(clazz: Class<T>): Boolean? = api.getViewClassSensitivity(clazz)
}
