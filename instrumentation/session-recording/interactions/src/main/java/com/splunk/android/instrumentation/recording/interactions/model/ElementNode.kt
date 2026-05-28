package com.splunk.android.instrumentation.recording.interactions.model

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

data class ElementNode(
    val view: Wireframe.Frame.Scene.Window.View,
    val positionInList: Int?,
    val fragmentTag: String?
) {

    override fun toString(): String {
        return "ElementNode(view.id=${view.id}, view.identity=${view.identity}, positionInList=$positionInList, fragmentTag=$fragmentTag)"
    }
}
