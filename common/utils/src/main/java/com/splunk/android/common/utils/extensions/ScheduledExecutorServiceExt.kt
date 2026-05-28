package com.splunk.android.common.utils.extensions

import com.splunk.android.common.utils.runOnUiThread
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

fun ScheduledExecutorService.safeSchedule(delayMs: Long, action: () -> Unit): ScheduledFuture<*> {
    val safeAction = {
        try {
            action()
        } catch (e: Exception) {
            val delegatedException = Exception("Exception catch in '${Thread.currentThread().name}' thread", e)
            runOnUiThread { throw delegatedException }
        }
    }

    return schedule(safeAction, delayMs, TimeUnit.MILLISECONDS)
}

fun <T> ScheduledExecutorService.safeScheduleWithFixedDelay(initialDelayMs: Long, delayMs: Long, block: () -> T): ScheduledFuture<*> {
    val safeBlock: () -> Unit = {
        try {
            block()
        } catch (e: Throwable) {
            val delegatedException = Exception("Exception catch in '${Thread.currentThread().name}' thread", e)
            runOnUiThread { throw delegatedException }
        }
    }

    return scheduleWithFixedDelay(safeBlock, initialDelayMs, delayMs, TimeUnit.MILLISECONDS)
}
