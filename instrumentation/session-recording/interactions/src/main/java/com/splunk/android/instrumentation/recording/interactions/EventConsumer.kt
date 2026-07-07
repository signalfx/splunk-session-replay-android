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

package com.splunk.android.instrumentation.recording.interactions

import android.app.Activity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal abstract class EventConsumer(
    val listener: OnInteractionListener
) {

    open fun onWireframeUpdated(frame: Wireframe.Frame) {}

    open fun onKeyEvent(rootView: View, event: KeyEvent) {}
    open fun onTouchEvent(rootView: View, targetView: View?, event: MotionEvent) {}
    open fun onMotionEvent(rootView: View, targetView: View?, event: MotionEvent) {}

    open fun onActivityStarted(activity: Activity) {}
    open fun onActivityResumed(activity: Activity) {}
    open fun onActivityPaused(activity: Activity) {}
    open fun onActivityStopped(activity: Activity) {}

    open fun onRootViewAdded(rootView: View) {}
    open fun onRootViewRemoved(rootView: View) {}
}
