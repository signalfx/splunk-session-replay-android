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
