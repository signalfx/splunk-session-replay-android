package com.splunk.android.instrumentation.recording.core.api.handler.impl

import android.view.View
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler
import com.splunk.android.instrumentation.recording.core.sensitivity.SensitivityHandler
import com.splunk.android.instrumentation.recording.core.sensitivity.sensitivityTag

internal class SensitivityApiHandlerImpl(
    private val sensitivityHandler: SensitivityHandler,
) : SensitivityApiHandler {

    //region Sensitivity set/get for Java

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

    //endregion
}
