package com.splunk.android.instrumentation.recording.core.api.handler.dummy

import android.view.View
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler

internal class SensitivityApiHandlerDummy : SensitivityApiHandler {

    override fun getViewInstanceSensitivity(view: View): Boolean? {
        return null
    }

    override fun setViewInstanceSensitivity(view: View, isSensitive: Boolean?) {}

    override fun <T : View> getViewClassSensitivity(clazz: Class<T>): Boolean? {
        return null
    }

    override fun <T : View> setViewClassSensitivity(clazz: Class<T>, isSensitive: Boolean?) {}
}
