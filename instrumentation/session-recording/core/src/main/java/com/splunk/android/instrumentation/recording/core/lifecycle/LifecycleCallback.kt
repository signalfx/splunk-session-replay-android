package com.splunk.android.instrumentation.recording.core.lifecycle

import android.app.Activity

internal abstract class LifecycleCallback {

    //region SDK lifecycle

    /**
     * Client wants to setup the SDK. Client is told to call this only in Applications
     * class onCreate() method. So this should be called on application start, if not, client is
     * using the SDK in non-intended way.
     */
    open fun onSetup() {}

    //endregion

    //region Activity lifecycle

    open fun onActivityStarted(activity: Activity) {}

    open fun onActivityStopped(activity: Activity) {}

    //endregion

    //region Application lifecycle

    /**
     * Application was closed and not opened again for minimum of:
     * [SDKLifecycleCallback.APPLICATION_SETTLE_EXECUTOR_DELAY] ms
     */
    open fun onApplicationClosedWithSettle() {}

    /**
     * Called when application crashed.
     */
    open fun onApplicationCrash(cause: Throwable) {}

    //endregion
}
