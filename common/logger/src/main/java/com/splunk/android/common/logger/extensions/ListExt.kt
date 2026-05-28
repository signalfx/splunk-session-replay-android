package com.splunk.android.common.logger.extensions

import android.text.SpannableStringBuilder
import com.splunk.android.common.logger.Log
import com.splunk.android.common.logger.formatter.DefaultLogFormatter
import com.splunk.android.common.logger.formatter.LogFormatter

/**
 * Format list of [Log] into user readable message.
 *
 * @param logLevel all logs on lower level will be ignored
 */
fun List<Log>.toUserMessage(
    logLevel: Log.Level = Log.Level.VERBOSE,
    formatter: LogFormatter = DefaultLogFormatter()
): CharSequence {
    return filter { it.level <= logLevel }
        .joinTo(
            buffer = SpannableStringBuilder(),
            separator = "\n"
        ) { formatter.format(it) }
}
