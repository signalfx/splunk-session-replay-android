package com.splunk.android.instrumentation.recording.wireframe.util

import android.graphics.Rect
import android.view.View
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

typealias ViewConsumer = (view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?) -> Window.View

typealias FragmentConsumer = (fragmentClass: Class<out Any>) -> Window.View.Type?
