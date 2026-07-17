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

package com.splunk.android.bridge

import android.view.View
import com.splunk.android.bridge.model.BridgeInterface
import com.splunk.rum.common.utils.MutableListObserver
import com.splunk.rum.common.utils.extensions.forEachFast

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
