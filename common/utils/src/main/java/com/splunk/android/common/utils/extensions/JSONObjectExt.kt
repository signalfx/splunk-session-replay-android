package com.splunk.android.common.utils.extensions

import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream

fun JSONObject.getFloat(name: String): Float {
    return getDouble(name).toFloat()
}

fun JSONObject.optStringNull(key: String): String? {
    return if (has(key)) {
        getString(key)
    } else null
}

fun JSONObject.optFloatNull(name: String): Float? {
    this.optDouble(name).let { number ->
        return if (number.isNaN()) {
            null
        } else {
            number.toFloat()
        }
    }
}

fun JSONObject.optLongNull(name: String): Long? =
    this.optFloatNull(name)?.toLong()

fun JSONObject.optBooleanNull(name: String): Boolean? {
    return if (has(name)) {
        optBoolean(name)
    } else {
        null
    }
}

fun <R> JSONObject.map(transformer: (json: JSONObject, key: String) -> R): List<R> {
    val result = ArrayList<R>()

    for (key in keys())
        result += transformer(this, key)

    return result
}

@Throws(IOException::class)
fun JSONObject.compress(): ByteArray {
    val content = toString()
    val stream = ByteArrayOutputStream(content.length)
    val deflater = Deflater(Deflater.BEST_COMPRESSION)
    val deflaterStream = DeflaterOutputStream(stream, deflater)

    deflaterStream.write(content.toByteArray())
    deflaterStream.close()
    deflater.end()

    return stream.toByteArray()
}
