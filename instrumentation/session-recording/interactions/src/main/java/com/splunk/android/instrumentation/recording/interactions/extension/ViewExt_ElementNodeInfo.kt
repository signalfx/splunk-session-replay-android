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

package com.splunk.android.instrumentation.recording.interactions.extension

import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.getFragmentContainerViewTag
import com.splunk.android.common.utils.extensions.getPositionInList
import com.splunk.android.common.utils.extensions.identity
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.interactions.model.ElementNodeInfo

// MARK Needs Proguard rules

private val FRAGMENT_CLASS = "androidx.fragment.app.Fragment".toClass()

internal fun View.createElementNodeInfoPath(): MutableList<ElementNodeInfo> {
    val nodes = ArrayList<ElementNodeInfo>()
    var currentView: View? = this

    while (currentView != null) {
        nodes += ElementNodeInfo(
            identity = currentView.identity,
            positionInList = currentView.getPositionInList(),
            fragmentTag = currentView.getFragmentTag()
        )

        currentView = currentView.parent as? View
    }

    nodes.reverse()
    return nodes
}

private fun View.getFragmentTag(): String? {
    if (this is ViewGroup) {
        val fragmentContainerViewTag = context.getFragmentContainerViewTag() ?: return null
        val fragment = getTag(fragmentContainerViewTag) ?: return null

        if (fragment::class.java == FRAGMENT_CLASS)
            return fragment.get("tag")
    }

    return null
}
