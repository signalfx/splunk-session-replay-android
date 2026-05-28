package com.splunk.android.common.job

sealed interface JobResult {
    object Success : JobResult
    data class Failure(val reason: FailureReason) : JobResult {
        enum class FailureReason {
            LIMIT_REACHED, UNKNOWN, SYSTEM_REJECTED
        }
    }
}
