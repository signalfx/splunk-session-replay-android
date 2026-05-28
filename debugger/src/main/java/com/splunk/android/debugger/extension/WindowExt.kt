package com.splunk.android.debugger.extension

import android.os.Build
import android.view.View
import android.view.Window

fun Window.setDecorFitsSystemWindowsCompat(decorFitsSystemWindows: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        setDecorFitsSystemWindows(decorFitsSystemWindows)
    else {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        val decorView = decorView
        val currentSysUiVis = decorView.systemUiVisibility

        decorView.systemUiVisibility = if (decorFitsSystemWindows) currentSysUiVis and flags.inv() else currentSysUiVis or flags
    }
}
