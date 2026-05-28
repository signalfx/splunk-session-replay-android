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
