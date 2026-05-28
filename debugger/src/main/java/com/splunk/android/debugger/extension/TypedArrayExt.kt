package com.splunk.android.debugger.extension

import android.content.res.TypedArray
import androidx.annotation.StyleableRes

internal inline fun <reified T : Enum<T>> TypedArray.getEnum(@StyleableRes index: Int, defValue: T?): T? {
    val ordinal = getInt(index, defValue?.ordinal ?: -1)
    return if (ordinal == -1)
        defValue
    else
        enumValues<T>()[ordinal]
}
