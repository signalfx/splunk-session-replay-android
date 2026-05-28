package com.splunk.android.instrumentation.recording.core.api

import android.view.View
import com.splunk.android.instrumentation.recording.core.dependencyInjection.DependencyInjectionTree
import kotlin.reflect.KClass

/**
 * Equivalent to [Sensitivity.getViewInstanceSensitivity] and [Sensitivity.setViewInstanceSensitivity].
 */
@get:JvmSynthetic
@set:JvmSynthetic
var View.isSensitive: Boolean?
    get() = DependencyInjectionTree.sensitivityApiHandler.getViewInstanceSensitivity(this)
    set(value) = DependencyInjectionTree.sensitivityApiHandler.setViewInstanceSensitivity(this, value)

/**
 * Equivalent to [Sensitivity.getViewClassSensitivity] and [Sensitivity.setViewClassSensitivity].
 */
@get:JvmSynthetic
@set:JvmSynthetic
var <T : View> KClass<T>.isSensitive: Boolean?
    get() = DependencyInjectionTree.sensitivityApiHandler.getViewClassSensitivity(this.java)
    set(value) = DependencyInjectionTree.sensitivityApiHandler.setViewClassSensitivity(this.java, value)

/**
 * Equivalent to [Sensitivity.getViewClassSensitivity] and [Sensitivity.setViewClassSensitivity].
 */
@get:JvmSynthetic
@set:JvmSynthetic
var <T : View> Class<T>.isSensitive: Boolean?
    get() = DependencyInjectionTree.sensitivityApiHandler.getViewClassSensitivity(this)
    set(value) = DependencyInjectionTree.sensitivityApiHandler.setViewClassSensitivity(this, value)
