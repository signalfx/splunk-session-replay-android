package com.splunk.android.common.job

interface IJobManager {
    fun scheduleJob(jobType: JobType): JobResult

    fun isJobScheduled(id: Int): Boolean

    fun cancelAll()

    fun cancel(id: Int)
}
