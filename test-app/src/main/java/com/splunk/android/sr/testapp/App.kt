package com.splunk.android.sr.testapp

import android.app.Application
import android.os.Build
import android.os.StrictMode
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.splunk.android.bridge.BridgeManager
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.logger.consumers.AndroidLogConsumer
import com.splunk.android.common.logger.consumers.SystemConsumer
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
