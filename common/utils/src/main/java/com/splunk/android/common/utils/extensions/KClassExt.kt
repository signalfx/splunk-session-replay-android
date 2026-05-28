package com.splunk.android.common.utils.extensions

import java.lang.reflect.Field
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun <T> KClass<*>.getStatic(fieldName: String): T? {
    val field = findField(fieldName)
    field.makeReadable()

    return field.get(null) as? T
}

fun <T : Any> KClass<*>.setStatic(fieldName: String, value: T?) {
    val field = findField(fieldName)
    field.makeWritable()

    field.set(null, value)
}

@Throws(NoSuchFieldException::class)
fun KClass<*>.findField(name: String): Field {
    return java.findField(name)
}
