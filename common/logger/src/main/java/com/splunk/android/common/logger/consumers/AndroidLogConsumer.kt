package com.splunk.android.common.logger.consumers

import com.splunk.android.common.logger.Log
import com.splunk.android.common.logger.Logger
import android.util.Log as AndroidLog

class AndroidLogConsumer : Logger.Consumer {

    override fun onNewLog(log: Log) {
        when (log.level) {
            Log.Level.ERROR -> AndroidLog.e(log.tag, log.message, log.throwable)
            Log.Level.WARN -> AndroidLog.w(log.tag, log.message, log.throwable)
            Log.Level.INFO -> AndroidLog.i(log.tag, log.message, log.throwable)
            Log.Level.DEBUG -> AndroidLog.d(log.tag, log.message, log.throwable)
            Log.Level.VERBOSE -> AndroidLog.v(log.tag, log.message, log.throwable)
        }
    }
}
