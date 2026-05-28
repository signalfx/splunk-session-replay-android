package com.splunk.android.common.utils.extensions

import java.lang.reflect.Field

val Any.simpleClassName: String
    get() = javaClass.simpleName

@Throws(NoSuchFieldException::class)
inline fun <reified T : Any> Any.invoke(methodName: String, vararg paramsPairs: Pair<Any?, Class<*>>): T? {
    val classes = Array(paramsPairs.size) { paramsPairs[it].second }
    val method = javaClass.findMethod(methodName, T::class.java, *classes)
    val params = Array(paramsPairs.size) { paramsPairs[it].first }

    return method.invoke(this, *params) as? T
}

fun <T : Any> Any.get(fieldName: String): T? {
    val field = javaClass.findField(fieldName)
    return get(field)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> Any.get(field: Field): T? {
    field.makeReadable()
    return field.get(this) as? T
}

fun <T : Any> Any.set(fieldName: String, value: T?) {
    val field = javaClass.findField(fieldName)
    set(field, value)
}

fun <T : Any> Any.set(field: Field, value: T?) {
    field.makeWritable()
    field.set(this, value)
}

val Any.identity: String
    get() = "${this::class.java.name}@${Integer.toHexString(System.identityHashCode(this))}"
