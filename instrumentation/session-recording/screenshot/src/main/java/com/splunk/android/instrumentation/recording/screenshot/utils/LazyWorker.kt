package com.splunk.android.instrumentation.recording.screenshot.utils

import java.util.concurrent.atomic.AtomicBoolean

internal class LazyWorker(name: String = "LazyWorker") {

    private val thread = WorkerThread(name)

    val isWorking: Boolean
        get() = thread.isWorking

    /**
     * Run process only when not working on previous one.
     *
     * @param preprocess runs before [process] in caller thread
     * @param process runs after [preprocess] in background thread
     *
     * @return whether the given [process] will be processed
     */
    fun submit(preprocess: () -> Boolean, process: () -> Unit): Boolean {
        val task = Task(preprocess, process)
        return thread.submit(task)
    }

    /**
     * Run process immediately or just after the current process.
     */
    fun submitPriority(process: () -> Unit) {
        thread.submitPriority(PriorityTask(process))
    }

    private inner class WorkerThread(name: String) : Thread(name) {

        private val lock = Object()
        private var isDead = false
        private var task: Task? = null
        private var priorityTask: PriorityTask? = null
        private val workStatus = AtomicBoolean()

        val isWorking: Boolean
            get() = synchronized(lock) { isAlive && !isDead && workStatus.get() }

        init {
            start()
        }

        fun submit(task: Task): Boolean {
            synchronized(lock) {
                if (isWorking)
                    return false

                return if (task.preprocess()) {
                    this.task = task
                    lock.notifyAll()
                    true
                } else
                    false
            }
        }

        fun submitPriority(task: PriorityTask) {
            synchronized(lock) {
                priorityTask = task
                lock.notifyAll()
            }
        }

        override fun run() {
            while (!isDead) {
                synchronized(lock) {
                    if (priorityTask == null && task == null) {
                        workStatus.set(false)
                        runCatching { lock.wait() }
                    }

                    workStatus.set(true)
                }

                priorityTask?.process?.invoke()
                priorityTask = null

                task?.process?.invoke()
                task = null
            }
        }
    }

    private class Task(
        val preprocess: () -> Boolean,
        val process: () -> Unit
    )

    private class PriorityTask(
        val process: () -> Unit
    )
}
