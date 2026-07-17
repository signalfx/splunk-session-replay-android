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

package com.splunk.android.sr.testapp

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.splunk.android.bridge.BridgeManager
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.logger.consumers.AndroidLogConsumer
import com.splunk.rum.common.logger.consumers.SystemConsumer
import com.splunk.android.sr.testapp.bridge.TomasBridgeInterface
import com.splunk.android.sr.testapp.util.AppPreferences

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        setup()
    }

    private fun setup() {
        AppPreferences.getInstance().load(this)

        BridgeManager.bridgeInterfaces += TomasBridgeInterface()

        Logger.consumers += if (BuildConfig.DEBUG) AndroidLogConsumer() else SystemConsumer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            setupStrictMode()

        Agent.init()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun setupStrictMode() {
        val showWarning = { message: String ->
            Toast.makeText(this, "🔴 STRICT MODE 🔴\n$message", Toast.LENGTH_LONG).show()
        }

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyListener(mainExecutor) {
                    showWarning("Thread: ${it.javaClass.simpleName}")
                }
                .build()
        )

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyListener(mainExecutor) {
                    showWarning("VM: ${it.javaClass.simpleName}")
                }
                .build()
        )
    }
}
