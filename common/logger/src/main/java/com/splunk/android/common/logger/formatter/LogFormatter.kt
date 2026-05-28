package com.splunk.android.common.logger.formatter

import com.splunk.android.common.logger.Log

/**
 * Format [Log] into message.
 */
interface LogFormatter {
    fun format(log: Log): CharSequence
}
