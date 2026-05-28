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
