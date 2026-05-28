package com.splunk.android.common.utils.extensions

import org.json.JSONArray

fun <T> Iterable<T>.toJSONArray(transformation: (array: JSONArray, item: T) -> Unit): JSONArray {
    val jsonArray = JSONArray()
    forEach { item -> transformation(jsonArray, item) }
    return jsonArray
}
