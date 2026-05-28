package com.splunk.android.common.utils.validation

import com.splunk.android.common.utils.validation.rules.Rule
import com.splunk.android.common.utils.validation.rulesets.Ruleset

object ValidationUtil {

    fun <T> validate(item: T?, ruleset: Ruleset<T>): Boolean {
        var isValid = true

        ruleset.rules.sortedByDescending { it.minimalEvaluation }.forEach { rule ->
            val result = rule.validate(item)
            if (result is Rule.Result.NotValid) {
                ruleset.onRuleFailure(result.cause)
                if (rule.minimalEvaluation) { return false }
                isValid = false
            }
        }

        return isValid
    }

    fun <T> validate(item: T?, rules: List<Rule<T>>): ValidationResult {
        val causes: MutableList<Rule.Cause> = mutableListOf()

        rules.sortedByDescending { it.minimalEvaluation }.forEach { rule ->
            val result = rule.validate(item)
            if (result is Rule.Result.NotValid) {
                causes += result.cause
                if (rule.minimalEvaluation) { return ValidationResult.NotValid(causes) }
            }
        }

        return if (causes.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.NotValid(causes)
        }
    }
}
