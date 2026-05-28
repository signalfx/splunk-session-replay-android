package com.splunk.android.common.utils.extensions

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun Long.toISO8601String(): String = createIso8601Format().format(this)

fun Date.toISO8601String(): String = createIso8601Format().format(this)

private fun createIso8601Format() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
