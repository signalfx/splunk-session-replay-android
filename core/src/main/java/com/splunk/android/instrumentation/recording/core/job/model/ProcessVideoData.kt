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

package com.splunk.android.instrumentation.recording.core.job.model

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.content.Context
import com.splunk.rum.common.job.JobType
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
