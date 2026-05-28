package com.splunk.android.instrumentation.recording.core.api.handler

import android.view.View

internal interface SensitivityApiHandler {

    fun getViewInstanceSensitivity(view: View): Boolean?

    fun setViewInstanceSensitivity(view: View, isSensitive: Boolean?)

    fun <T : View> getViewClassSensitivity(clazz: Class<T>): Boolean?

    fun <T : View> setViewClassSensitivity(clazz: Class<T>, isSensitive: Boolean?)
}
