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

package com.splunk.android.instrumentation.recording.core

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Rect
import android.os.Build
import android.view.View
import com.splunk.android.common.utils.Region
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.capturer.ScreenMasksProvider
import com.splunk.android.instrumentation.recording.core.api.RecordingMask
import com.splunk.android.instrumentation.recording.core.api.SessionReplay
import com.splunk.android.instrumentation.recording.core.api.handler.dummy.CoreApiHandlerDummy
import com.splunk.android.instrumentation.recording.core.api.handler.dummy.PreferencesApiHandlerDummy
import com.splunk.android.instrumentation.recording.core.api.handler.dummy.SensitivityApiHandlerDummy
import com.splunk.android.instrumentation.recording.core.api.handler.dummy.StateApiHandlerDummy
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.instrumentation.recording.core.dependencyInjection.DependencyInjectionTree
import com.splunk.android.instrumentation.recording.interactions.Interactions
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import com.splunk.android.instrumentation.recording.wireframe.model.SensitivityDeterminer
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats

internal object Initializer {
    private val dummyInstance: SessionReplay by lazy {
        SessionReplay(
            CoreApiHandlerDummy(),
            PreferencesApiHandlerDummy(),
            StateApiHandlerDummy(),
            SensitivityApiHandlerDummy()
        )
    }

    private val correctInstance: SessionReplay by lazy {
        SessionReplay(
            DependencyInjectionTree.coreApiHandler,
            DependencyInjectionTree.preferencesApiHandler,
            DependencyInjectionTree.stateApiHandler,
            DependencyInjectionTree.sensitivityApiHandler
        )
    }

    internal lateinit var application: Application
    internal var setupTimestamp: Long? = null

    internal val instance: SessionReplay
        get() = if (this::application.isInitialized) correctInstance else dummyInstance

    @SuppressLint("NewApi")
    fun setup(application: Application) {
        if (Build.VERSION.SDK_INT <= Constants.MIN_ANDROID_SDK)
            return

        this.application = application
        this.setupTimestamp = System.currentTimeMillis()

        val lifecycleHandler = DependencyInjectionTree.lifecycleHandler
        lifecycleHandler.handlesLifecycleList += DependencyInjectionTree.screenCapturer
        lifecycleHandler.setup(application)

        FrameCapturer.frameHolder.screenshotsCountLimit = 2
        FrameCapturer.attach(application)
        Interactions.attach(application)

        FrameCapturer.listeners += object : FrameCapturer.Listener {
            override fun onNewWireframe(frame: Wireframe.Frame, stats: WireframeStats) {
                Interactions.updateWireframe(frame)
            }
        }

        WireframeExtractor.sensitivityDeterminer = object : SensitivityDeterminer {
            override fun isViewSensitive(view: View): Boolean? {
                return view.isSensitive ?: view::class.java.isSensitiveInHierarchy
            }

            private val <T : View> Class<T>.isSensitiveInHierarchy: Boolean?
                get() {
                    var clazz: Class<T>? = this
                    var isSensitive = clazz?.isSensitive

                    while (clazz != null && isSensitive == null) {
                        clazz = clazz.superclass as? Class<T>
                        isSensitive = clazz?.isSensitive
                    }

                    return isSensitive
                }
        }

        FrameCapturer.screenMasksProvider = object : ScreenMasksProvider { // TODO Cache result, invalidate when recordingMask.elements was changed

            private val region = Region()

            override fun onScreenMasksRequested(): List<Rect> {
                val elements = SessionReplay.instance.recordingMask?.elements?.takeIf { it.isNotEmpty() } ?: return emptyList()

                region.reset()

                for (element in elements)
                    when (element.type) {
                        RecordingMask.Element.Type.COVERING ->
                            region.addArea(element.rect)
                        RecordingMask.Element.Type.ERASING ->
                            region.clipOut(element.rect)
                    }

                return region.getResult()
            }
        }
    }
}
