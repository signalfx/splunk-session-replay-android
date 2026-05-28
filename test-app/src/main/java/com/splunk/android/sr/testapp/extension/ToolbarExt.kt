package com.splunk.android.sr.testapp.extension

import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar

fun Toolbar.setSubtitle(@StringRes resId: Int?) {
    if (resId != null)
        setSubtitle(resId)
    else
        subtitle = ""
}
