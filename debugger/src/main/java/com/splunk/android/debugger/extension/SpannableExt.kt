package com.splunk.android.debugger.extension

import android.text.Spannable
import android.text.Spanned

internal operator fun Spannable.set(start: Int, end: Int, span: Any) {
    setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
}
