package com.splunk.android.instrumentation.recording.screenshot.extension

internal fun <K, V> Map<K, V>.asMutableMap(): MutableMap<K, V> {
    return (this as? MutableMap<K, V>) ?: toMutableMap()
}
