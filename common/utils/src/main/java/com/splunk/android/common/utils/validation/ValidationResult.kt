package com.splunk.android.common.utils.validation

import com.splunk.android.common.utils.validation.rules.Rule

sealed interface ValidationResult {
    object Valid : ValidationResult
    class NotValid(val causes: List<Rule.Cause>) : ValidationResult
}
