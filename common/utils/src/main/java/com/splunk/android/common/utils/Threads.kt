package com.splunk.android.common.utils

import android.os.Handler
import android.os.Looper
import com.splunk.android.common.utils.extensions.barrier
import com.splunk.android.common.utils.extensions.safeInvokeAll
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private val mainHandler = Handler(Looper.getMainLooper())

fun runOnUiThread(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper())
        block()
    else
        mainHandler.post(block)
}

fun runOnBackgroundThread(
    name: String = "BackgroundWorker",
    uncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null,
    block: () -> Unit
): Thread {
    val thread = Thread(block, name)
    thread.uncaughtExceptionHandler = uncaughtExceptionHandler
    thread.start()
    return thread
}

fun <R> runOnBackgroundThreadInParallel(tasks: List<() -> R>, threadCount: Int): List<R> {
    val executors = Executors.newFixedThreadPool(threadCount)
    val callables = tasks.map { task -> Callable { task() } }

    return executors.safeInvokeAll(callables)
        .map { future -> future.get() }
}

@OptIn(ExperimentalContracts::class)
fun runOnUiThreadSync(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }

    return if (Looper.myLooper() == Looper.getMainLooper())
        block()
    else {
        barrier(1) {
            mainHandler.post {
                block()
                it.decrease()
            }
        }
    }
}
