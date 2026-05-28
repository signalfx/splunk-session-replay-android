package com.splunk.android.common.utils.extensions

import android.app.Activity
import android.view.View
import android.view.ViewGroup

val Activity.contentView: ViewGroup?
    get() = findViewById(android.R.id.content)

val Activity.rootView: View?
    get() = contentView?.rootView
