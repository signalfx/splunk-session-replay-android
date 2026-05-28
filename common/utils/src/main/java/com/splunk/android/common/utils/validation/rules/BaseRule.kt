package com.splunk.android.common.utils.validation.rules

abstract class BaseRule<T>(minimalEvaluation: Boolean = false) : Rule<T>(minimalEvaluation) {

    class NotNull<T> : BaseRule<T>(true) {
        override fun validate(item: T?): Result {
            return if (item != null) {
                Result.Valid
            } else {
                Result.NotValid(Cause.Null)
            }
        }
    }

    /**
     * Validation failure causes.
     */
    interface Cause : Rule.Cause {
        object Null : Cause
    }
}
