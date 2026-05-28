package com.splunk.android.common.utils.validation.rulesets

import com.splunk.android.common.utils.validation.rules.Rule

interface Ruleset<T> {
    val rules: Set<Rule<T>>
    fun onRuleFailure(cause: Rule.Cause)
}
