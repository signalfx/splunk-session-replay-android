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
