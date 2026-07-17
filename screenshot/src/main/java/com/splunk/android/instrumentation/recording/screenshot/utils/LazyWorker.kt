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
