package com.splunk.android.sr.testapp.ui.wireframe.model

import android.graphics.Color

data class SimpleItem(
    val id: Int,
    val title: String,
    val description: String,
    val backgroundColor: Int = Color.TRANSPARENT
)
