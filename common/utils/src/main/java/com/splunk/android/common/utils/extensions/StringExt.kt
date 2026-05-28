package com.splunk.android.common.utils.extensions

import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass

fun String.toKClass(): KClass<*>? {
    return toClass()?.kotlin
}

fun String.toJSONObject(): JSONObject {
    return JSONObject(this)
}

fun String.toJSONArray(): JSONArray {
    return JSONArray(this)
}

fun String.toClass(): Class<*>? {
    return try {
        Class.forName(this)
    } catch (_: Exception) {
        null
    }
}
