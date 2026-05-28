package com.splunk.android.debugger.extension

import android.content.ContentValues

internal operator fun ContentValues.set(key: String, value: String?) {
    put(key, value)
}
