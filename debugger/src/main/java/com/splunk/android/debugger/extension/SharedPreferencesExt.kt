/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.splunk.android.debugger.extension

import android.content.SharedPreferences

@JvmName("getEnumNullable")
internal inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, defValue: T?): T? { // FIXME contract returnsNotNull is broken at the moment
    val name = getString(key, null) ?: return defValue
    return enumValues<T>().find { it.name == name } ?: return defValue
}

internal inline fun <reified T : Enum<T>> SharedPreferences.getEnum(key: String, defValue: T): T { // FIXME contract returnsNotNull is broken at the moment
    val name = getString(key, null) ?: return defValue
    return enumValues<T>().find { it.name == name } ?: return defValue
}

internal fun <T : Enum<T>> SharedPreferences.Editor.putEnum(key: String, value: T?): SharedPreferences.Editor {
    return putString(key, value?.name)
}
