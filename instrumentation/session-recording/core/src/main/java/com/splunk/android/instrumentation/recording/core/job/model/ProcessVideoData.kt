package com.splunk.android.instrumentation.recording.core.job.model

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.content.Context
import com.splunk.android.common.job.JobType
import com.splunk.android.instrumentation.recording.core.dependencyInjection.DependencyInjectionTree
import com.splunk.android.instrumentation.recording.core.job.worker.ProcessVideoDataJob

@SuppressLint("NewApi")
internal data class ProcessVideoData(val data: ProcessVideoDataJobData) : JobType {
    override val jobNumberLimit: Long = ProcessVideoDataJob.JOB_NUMBER_LIMIT
    override fun createJobInfo(context: Context): JobInfo {
        return ProcessVideoDataJob.createJobInfoBuilder(
            context,
            DependencyInjectionTree.jobIdStorage.getOrCreateId(data.dataChunkId),
            data
        ).build()
    }
}
