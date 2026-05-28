package com.splunk.android.instrumentation.recording.wireframe.identifier

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

interface FragmentTypeIdentifier {

    fun identify(fragmentClass: Class<out Any>): Window.View.Type?
}
