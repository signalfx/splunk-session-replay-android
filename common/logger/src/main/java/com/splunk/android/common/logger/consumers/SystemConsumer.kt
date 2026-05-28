package com.splunk.android.common.logger.consumers

import com.splunk.android.common.logger.Log
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.logger.formatter.DefaultLogFormatter
import com.splunk.android.common.logger.formatter.LogFormatter

class SystemConsumer(
    private val formatter: LogFormatter = DefaultLogFormatter(false)
) : Logger.Consumer {

    override fun onNewLog(log: Log) {
        val line = formatter.format(log)

        when (log.level) {
            Log.Level.ERROR -> System.err.println(line)
            Log.Level.WARN, Log.Level.INFO, Log.Level.DEBUG, Log.Level.VERBOSE -> println(line)
        }
    }
}
