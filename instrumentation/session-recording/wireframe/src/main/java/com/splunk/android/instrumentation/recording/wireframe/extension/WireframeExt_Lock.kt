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

package com.splunk.android.instrumentation.recording.wireframe.extension

import androidx.annotation.WorkerThread
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

/**
 * @return whether any part of wireframe was locked and could be updated.
 */
@Suppress("unused")
@WorkerThread
fun Wireframe.waitToFinish(): Boolean {
    var isUpdated = false

    frames.forEachFast { frame ->
        if (frame.waitToFinish())
            isUpdated = true
    }

    return isUpdated
}

/**
 * @return whether any part of wireframe was locked and could be updated.
 */
@WorkerThread
fun Wireframe.Frame.waitToFinish(): Boolean {
    var isUpdated = false

    scenes.forEachFast { scene ->
        scene.windows.forEachFast { window ->
            if (window.waitToFinish())
                isUpdated = true
        }
    }

    return isUpdated
}

/**
 * @return whether any part of wireframe was locked and could be updated.
 */
@WorkerThread
fun Wireframe.Frame.Scene.Window.waitToFinish(): Boolean {
    var isUpdated = false

    forEachView {
        val lock = it.subviewsLock

        if (lock != null) {
            lock.waitToUnlock()
            isUpdated = true
        }
    }

    return isUpdated
}
