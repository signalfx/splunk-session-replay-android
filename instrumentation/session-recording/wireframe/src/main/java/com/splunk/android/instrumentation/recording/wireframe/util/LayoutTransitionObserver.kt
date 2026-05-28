package com.splunk.android.instrumentation.recording.wireframe.util

import android.animation.LayoutTransition
import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.noneFast
import com.splunk.android.instrumentation.recording.wireframe.R

internal var View.isRunningVisibilityAnimation: Boolean
    get() = getTag(R.id.sr_tag_running_visibility_animation) as? Boolean ?: false
    set(value) = setTag(R.id.sr_tag_running_visibility_animation, value)

internal object LayoutTransitionObserver {

    fun listenTransitions(view: View) {
        val parent = view.parent as? ViewGroup ?: return
        val layoutTransition = parent.layoutTransition ?: return

        if (layoutTransition.transitionListeners == null || layoutTransition.transitionListeners.noneFast { it is TransitionListener })
            layoutTransition.addTransitionListener(TransitionListener())
    }

    private class TransitionListener : LayoutTransition.TransitionListener {
        override fun startTransition(transition: LayoutTransition, container: ViewGroup, view: View, transitionType: Int) {
            if (view.visibility != View.VISIBLE && transitionType == LayoutTransition.DISAPPEARING)
                view.isRunningVisibilityAnimation = true
        }

        override fun endTransition(transition: LayoutTransition, container: ViewGroup, view: View, transitionType: Int) {
            if (view.visibility != View.VISIBLE && transitionType == LayoutTransition.DISAPPEARING)
                view.isRunningVisibilityAnimation = false
        }
    }
}
