package com.splunk.android.common.utils.validation.extensions

import com.splunk.android.common.utils.validation.ValidationUtil
import com.splunk.android.common.utils.validation.rulesets.Ruleset

inline fun <T> runIfValid(item: T?, ruleset: Ruleset<T>, crossinline operation: () -> Unit) {
    if (ValidationUtil.validate(item, ruleset)) {
        operation()
    }
}

inline fun <T> runIfValid(vararg itemAndRulesetPairs: Pair<T?, Ruleset<T>>, crossinline operation: () -> Unit) {
    if (itemAndRulesetPairs.all { ValidationUtil.validate(it.first, it.second) }) {
        operation()
    }
}

fun <T> validate(item: T?, ruleset: Ruleset<T>): Boolean = ValidationUtil.validate(item, ruleset)

fun <T> validate(vararg itemAndRulesetPairs: Pair<T?, Ruleset<T>>): Boolean =
    itemAndRulesetPairs.all { ValidationUtil.validate(it.first, it.second) }
