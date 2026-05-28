package com.splunk.android.common.utils.extensions

import android.os.Build
import android.view.View
import android.view.ViewTreeObserver

fun View.doOnDraw(action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        doOnDraw29(action)
    else
        doOnDraw20(action)
}

private inline fun View.doOnDraw20(crossinline action: () -> Unit) {
    val onPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action()

            if (viewTreeObserver.isAlive)
                rootView.viewTreeObserver.removeOnPreDrawListener(this)

            return true
        }
    }

    viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
}

private inline fun View.doOnDraw29(crossinline action: () -> Unit) {
    var pendingRemove = false

    val onDrawListener = ViewTreeObserver.OnDrawListener {
        pendingRemove = true
        action()
    }

    val onPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            if (pendingRemove && viewTreeObserver.isAlive) {
                rootView.viewTreeObserver.removeOnDrawListener(onDrawListener)
                rootView.viewTreeObserver.removeOnPreDrawListener(this)
            }

            return true
        }
    }

    viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
    viewTreeObserver.addOnDrawListener(onDrawListener)
}
