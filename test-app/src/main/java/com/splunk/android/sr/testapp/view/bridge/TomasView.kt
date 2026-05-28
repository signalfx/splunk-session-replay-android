package com.splunk.android.sr.testapp.view.bridge

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class TomasView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val textureView = TomasTextureView(context)

    val elements: List<TomasElement>
        get() = textureView.elements

    var listener: Listener? = null

    init {
        textureView.listener = TomasTextureViewListener()
        addView(textureView)
    }

    private inner class TomasTextureViewListener : TomasTextureView.Listener {
        override fun onTransitionChanged(isRunning: Boolean) {
            listener?.onTransitionChanged(isRunning)
        }
    }

    interface Listener {
        fun onTransitionChanged(isRunning: Boolean)
    }
}
