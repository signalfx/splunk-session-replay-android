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
import android.app.Application
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.adapters.ActivityLifecycleCallbacksAdapter
import com.splunk.rum.common.utils.extensions.forEachFast
import com.splunk.rum.common.utils.extensions.safeSchedule
import com.splunk.rum.common.utils.extensions.simpleClassName
import com.splunk.rum.common.utils.thread.NamedThreadFactory
import com.splunk.android.instrumentation.recording.core.display.DisplayHandler
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.atomic.AtomicBoolean

internal class LifecycleHandler : ILifecycleHandler {
    companion object {
        private const val TAG = "SDKLifecycleHandler"
        private const val NO_ACTIVITIES = 0

        // Lifecycle executor settings
        private const val APPLICATION_SETTLE_EXECUTOR_POOL_SIZE = 2
        private const val APPLICATION_SETTLE_EXECUTOR_DELAY = 1000L // milliseconds
    }

    override val handlesLifecycleList: MutableList<HandlesLifecycle> = mutableListOf()
    val handlesLifecycleCallbacks: MutableList<LifecycleCallback> = mutableListOf()

    // Application settle executors
    private var applicationSettleExecutor: ScheduledExecutorService? = null
    private var settleFutures = mutableListOf<Future<*>>()

    // Activity counting
    private var activityCounter = NO_ACTIVITIES
    private var startedActivities = mutableListOf<String>()

    // Atomic locks
    private val recordingShouldRun = AtomicBoolean(false)
    private val realScreenSizeSetup = AtomicBoolean(false)

    // Weak activity reference
    private var weakActivity: WeakReference<Activity>? = null

    override fun setup(applicationContext: Application) {

        handlesLifecycleList.forEachFast { handlesLifecycleCallbacks.add(it.registerLifecycleCallback()) }

        handlesLifecycleCallbacks.forEachFast { it.onSetup() }

        val lifecycleCallback = object : ActivityLifecycleCallbacksAdapter {

            override fun onActivityStarted(activity: Activity) {
                Logger.d(TAG, "onActivityStarted() called with: activity = $activity")

                setupRealScreenSize(activity)
                weakActivity = WeakReference(activity)

                handlesLifecycleCallbacks.forEachFast { it.onActivityStarted(activity) }

                increaseActivityCounter(activity.simpleClassName)
            }

            override fun onActivityStopped(activity: Activity) {
                Logger.d(TAG, "onActivityStopped() called with: activity = $activity")

                handlesLifecycleCallbacks.forEachFast { it.onActivityStopped(activity) }

                decreaseActivityCounter(activity.simpleClassName)
            }
        }

        applicationContext.registerActivityLifecycleCallbacks(lifecycleCallback)
    }

    //endregion

    //region Start/Stop recording

    override fun startRecording() {
        Logger.d(TAG, "startRecording() called")

        weakActivity?.get()?.let { increaseActivityCounter(it.simpleClassName) }
        recordingShouldRun.set(true)
    }

    override fun stopRecording() {
        Logger.d(TAG, "stopRecording() called")

        clearActivityCounter()
        recordingShouldRun.set(false)
    }

    //endregion

    //region Crash callback

    override fun applicationCrash(cause: Throwable) {
        Logger.d(TAG, "applicationCrash() called with: cause = $cause")

        handlesLifecycleCallbacks.forEachFast { it.onApplicationCrash(cause) }
    }

    //endregion

    //region Activity counter

    private fun increaseActivityCounter(activityName: String) {
        Logger.d(TAG, "increaseCounter() called: activityName = $activityName, activityCounter = $activityCounter, startedActivities = $startedActivities")

        if (startedActivities.none { it == activityName }) {
            activityCounter++
            startedActivities.add(activityName)

            Logger.d(TAG, "increaseCounter() incremented with activity start: activityName = $activityName, activityCounter = $activityCounter, startedActivities = $startedActivities")

            if (activityCounter > NO_ACTIVITIES && applicationSettleExecutor != null) {
                Logger.d(TAG, "increaseCounter() called: shutdown application settle executor")

                applicationSettleExecutor?.shutdownNow()
                settleFutures.forEach { it.cancel(true) }
                settleFutures = ArrayList()
                applicationSettleExecutor = null
            }
        } else {
            Logger.d(TAG, "increaseCounter() activity already processed!")
        }
    }

    private fun decreaseActivityCounter(activityName: String) {
        Logger.d(TAG, "decreaseCounter() called with: activityName = $activityName, activityCounter = $activityCounter, startedActivities = $startedActivities")

        if (startedActivities.any { it == activityName }) {

            startedActivities.remove(activityName)
            activityCounter--

            Logger.d(TAG, "decreaseCounter() decremented with activity stop: activityName = $activityName, activityCounter = $activityCounter, startedActivities = $startedActivities")

            if (activityCounter == NO_ACTIVITIES && recordingShouldRun.get()) {
                letApplicationSettle()
            }
        } else {
            Logger.d(TAG, "decreaseCounter() activity started outside SDK recording!")
        }
    }

    private fun clearActivityCounter() {
        activityCounter = NO_ACTIVITIES
        startedActivities.clear()
    }

    /**
     * Solves edge case in which Android framework is laggy or client starts activity asynchronously.
     * We gonna wait additional APPLICATION_SETTLE_EXECUTOR_DELAY milliseconds before ending a session
     * just in case new activity is gonna be started.
     */
    private fun letApplicationSettle() {
        Logger.d(TAG, "letApplicationSettle(): application is going to settle")

        if (applicationSettleExecutor != null || !recordingShouldRun.get()) {
            return
        }

        val executor = Executors.newScheduledThreadPool(APPLICATION_SETTLE_EXECUTOR_POOL_SIZE, NamedThreadFactory("settle"))
        applicationSettleExecutor = executor

        settleFutures += executor.safeSchedule(APPLICATION_SETTLE_EXECUTOR_DELAY) {
            Logger.d(TAG, "letApplicationSettle(): application is settled and its closed")

            handlesLifecycleCallbacks.forEachFast { it.onApplicationClosedWithSettle() }
        }
    }

    //endregion

    /**
     * We are solving incorrect display size problem with this:
     * https://stackoverflow.com/questions/47682826/galaxy-s8-screen-height-returning-wrong-value/50924868
     */
    private fun setupRealScreenSize(activity: Activity) {
        if (!realScreenSizeSetup.get()) {
            DisplayHandler.setupRealDisplaySize(activity)
            realScreenSizeSetup.set(true)
        }
    }
}
