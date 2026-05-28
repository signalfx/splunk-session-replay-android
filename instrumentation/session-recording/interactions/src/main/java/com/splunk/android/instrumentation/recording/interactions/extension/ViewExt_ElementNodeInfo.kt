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
