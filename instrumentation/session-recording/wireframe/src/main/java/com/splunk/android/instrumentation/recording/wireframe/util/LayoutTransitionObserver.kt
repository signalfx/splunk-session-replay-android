/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
