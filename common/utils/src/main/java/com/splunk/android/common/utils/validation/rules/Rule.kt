package com.splunk.android.common.utils.validation.rules

abstract class Rule<T>(val minimalEvaluation: Boolean = false) {
    abstract fun validate(item: T?): Result

    sealed interface Result {
        object Valid : Result
        class NotValid(val cause: Cause) : Result
    }

    interface Cause
}
