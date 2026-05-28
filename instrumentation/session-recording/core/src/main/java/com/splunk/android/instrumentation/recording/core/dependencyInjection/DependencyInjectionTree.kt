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

package com.splunk.android.instrumentation.recording.core.dependencyInjection

import com.splunk.android.common.job.IJobManager
import com.splunk.android.common.job.JobIdStorage
import com.splunk.android.common.job.JobManager
import com.splunk.android.instrumentation.recording.core.video.ScreenCapturer
import com.splunk.android.instrumentation.recording.core.Core
import com.splunk.android.instrumentation.recording.core.Initializer
import com.splunk.android.instrumentation.recording.core.api.handler.CoreApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.PreferencesApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.SensitivityApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.StateApiHandler
import com.splunk.android.instrumentation.recording.core.api.handler.impl.CoreApiHandlerImpl
import com.splunk.android.instrumentation.recording.core.api.handler.impl.PreferencesApiHandlerImpl
import com.splunk.android.instrumentation.recording.core.api.handler.impl.SensitivityApiHandlerImpl
import com.splunk.android.instrumentation.recording.core.api.handler.impl.StateApiHandlerImpl
import com.splunk.android.instrumentation.recording.core.configuration.ConfigurationHandler
import com.splunk.android.instrumentation.recording.core.configuration.IConfigurationHandler
import com.splunk.android.instrumentation.recording.core.crash.CrashHandler
import com.splunk.android.instrumentation.recording.core.exporter.IDataChunkExporter
import com.splunk.android.instrumentation.recording.core.exporter.DataChunkExporter
import com.splunk.android.instrumentation.recording.core.lifecycle.ILifecycleHandler
import com.splunk.android.instrumentation.recording.core.lifecycle.LifecycleHandler
import com.splunk.android.instrumentation.recording.core.metadata.IMetadataHandler
import com.splunk.android.instrumentation.recording.core.metadata.MetadataHandler
import com.splunk.android.instrumentation.recording.core.sensitivity.SensitivityHandler
import com.splunk.android.instrumentation.recording.core.storage.ISessionReplayStorage
import com.splunk.android.instrumentation.recording.core.storage.SessionReplayStorage
import com.splunk.android.instrumentation.recording.core.video.ActiveDataChunkHandler
import com.splunk.android.instrumentation.recording.core.video.ClosedDataChunkHandler
import com.splunk.android.instrumentation.recording.core.video.EncoderQueue
import com.splunk.android.instrumentation.recording.core.video.IScreenCapturer

internal object DependencyInjectionTree {

    val lifecycleHandler: ILifecycleHandler by lazy { LifecycleHandler() }

    val metadataHandler: IMetadataHandler by lazy { MetadataHandler }

    val configurationHandler: IConfigurationHandler by lazy {
        ConfigurationHandler(
            storage = storage,
            jobIdStorage = jobIdStorage,
            jobManager = jobManager
        )
    }

    val storage: ISessionReplayStorage by lazy { SessionReplayStorage(context = Initializer.application, isEncrypted = false) }

    val sensitivityHandler by lazy { SensitivityHandler() }

    val encoderQueue: EncoderQueue by lazy { EncoderQueue(storage = storage) }

    val dataChunkExporter: IDataChunkExporter by lazy { DataChunkExporter(storage = storage) }

    val activeDataChunkHandler: ActiveDataChunkHandler by lazy {
        ActiveDataChunkHandler(
            encoderQueue = encoderQueue,
            storage = storage,
            dataChunkExporter = dataChunkExporter,
        )
    }

    val closedDataChunkHandler: ClosedDataChunkHandler by lazy {
        ClosedDataChunkHandler(
            encoderQueue = encoderQueue,
            storage = storage,
            dataChunkExporter = dataChunkExporter,
        )
    }

    val screenCapturer: IScreenCapturer by lazy {
        ScreenCapturer(
            configurationHandler = configurationHandler,
            storage = storage,
            activeDataChunkHandler = activeDataChunkHandler,
            closedDataChunkHandler = closedDataChunkHandler
        )
    }

    val jobIdStorage by lazy { JobIdStorage.init(Initializer.application, isEncrypted = false) }
    val jobManager: IJobManager by lazy { JobManager.attach(Initializer.application) }

    val core by lazy {
        Core(
            screenCapturer = screenCapturer,
            lifecycleHandler = lifecycleHandler,
            metadataHandler = metadataHandler,
            configurationHandler = configurationHandler,
            crashHandler = crashHandler
        )
    }

    val crashHandler: CrashHandler by lazy { CrashHandler(lifecycleHandler = lifecycleHandler) }

    val coreApiHandler: CoreApiHandler by lazy { CoreApiHandlerImpl(core = core) }
    val preferencesApiHandler: PreferencesApiHandler by lazy { PreferencesApiHandlerImpl(configurationHandler = configurationHandler) }
    val sensitivityApiHandler: SensitivityApiHandler by lazy { SensitivityApiHandlerImpl(sensitivityHandler = sensitivityHandler) }
    val stateApiHandler: StateApiHandler by lazy { StateApiHandlerImpl(configurationHandler = configurationHandler, core = core) }
}
