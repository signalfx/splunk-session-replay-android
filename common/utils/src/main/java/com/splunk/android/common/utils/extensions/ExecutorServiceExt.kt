package com.splunk.android.common.utils.extensions

import com.splunk.android.common.utils.runOnUiThread
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

fun ExecutorService.safeSubmit(block: () -> Unit): Future<*> {
    val safeBlock = {
        try {
            block()
        } catch (e: Throwable) {
            val delegatedException = Exception("Exception catch in '${Thread.currentThread().name}' thread", e)
            runOnUiThread { throw delegatedException }
        }
    }

    return submit(safeBlock)
}

fun <T> ExecutorService.safeInvokeAll(tasks: Collection<Callable<T>>): List<Future<T>> {
    val safeTasks = tasks.map {
        Callable {
            try {
                it.call()
            } catch (e: Throwable) {
                val delegatedException = Exception("Exception catch in '${Thread.currentThread().name}' thread", e)
                runOnUiThread { throw delegatedException }

                throw e // This will be consumed by ExecutorService
            }
        }
    }

    return invokeAll(safeTasks)
}
