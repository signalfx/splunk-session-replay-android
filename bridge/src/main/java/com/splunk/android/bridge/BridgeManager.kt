package com.splunk.android.bridge

import android.view.View
import com.splunk.android.bridge.model.BridgeInterface
import com.splunk.android.common.utils.MutableListObserver
import com.splunk.android.common.utils.extensions.forEachFast

object BridgeManager {

    /**
     * [BridgeInterface] collection where each item describes framework's [View]s.
     */
    val bridgeInterfaces: MutableCollection<BridgeInterface> = MutableListObserver(ArrayList(), BridgeInterfaceObserver())

    val listeners: MutableCollection<Listener> = ArrayList()

    val isRecordingAllowed: Boolean
        get() = bridgeInterfaces.all { it.isRecordingAllowed }

    private class BridgeInterfaceObserver : MutableListObserver.Observer<BridgeInterface> {
        override fun onAdded(element: BridgeInterface) {
            listeners.forEachFast { it.onBridgeInterfaceAdded(element) }
        }

        override fun onRemoved(element: BridgeInterface) {
            listeners.forEachFast { it.onBridgeInterfaceRemoved(element) }
        }
    }

    interface Listener {
        fun onBridgeInterfaceAdded(bridge: BridgeInterface) {}
        fun onBridgeInterfaceRemoved(bridge: BridgeInterface) {}
    }
}
