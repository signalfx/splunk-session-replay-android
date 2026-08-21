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

package com.splunk.android.instrumentation.recording.core.api.handler.impl

import android.view.View
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler
import com.splunk.android.instrumentation.recording.core.sensitivity.SensitivityHandler
import com.splunk.android.instrumentation.recording.core.sensitivity.sensitivityTag

internal class SensitivityApiHandlerImpl(
    private val sensitivityHandler: SensitivityHandler,
) : SensitivityApiHandler {

    override fun getViewInstanceSensitivity(view: View): Boolean? {
        return view.sensitivityTag
    }

    override fun setViewInstanceSensitivity(view: View, isSensitive: Boolean?) {
        view.sensitivityTag = isSensitive
    }

    override fun <T : View> getViewClassSensitivity(clazz: Class<T>): Boolean? {
        return sensitivityHandler.sensitiveClasses[clazz]
    }

    override fun <T : View> setViewClassSensitivity(clazz: Class<T>, isSensitive: Boolean?) {
        sensitivityHandler.sensitiveClasses[clazz] = isSensitive
    }

    override fun getComposeTextFieldSensitivity(): Boolean? {
        return sensitivityHandler.composeTextFieldSensitivity
    }

    override fun setComposeTextFieldSensitivity(isSensitive: Boolean?) {
        sensitivityHandler.composeTextFieldSensitivity = isSensitive
    }
}
