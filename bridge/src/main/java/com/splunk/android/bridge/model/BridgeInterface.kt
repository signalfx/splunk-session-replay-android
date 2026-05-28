package com.splunk.android.bridge.model

import android.view.View

interface BridgeInterface {

    val isRecordingAllowed: Boolean

    /**
     * Returns basic info of the framework. Result should by cached by the implementation.
     */
    fun obtainFrameworkInfo(callback: (BridgeFrameworkInfo?) -> Unit)

    /**
     * Returns classes that can be described by the implementation.
     */
    fun obtainWireframeRootClasses(): List<Class<out View>>

    /**
     * Returns wireframe description. Implementation should do as little work as possible on the main thread.
     *
     * @param instance element instance
     * @param callback used to provide result, must always be called
     */
    fun obtainWireframeData(instance: View, callback: (BridgeWireframe?) -> Unit)
}
