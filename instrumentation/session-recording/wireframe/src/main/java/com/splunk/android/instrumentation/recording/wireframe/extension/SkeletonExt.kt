package com.splunk.android.instrumentation.recording.wireframe.extension

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton

internal val WireframeSkeleton.isSimpleRect: Boolean
    get() = this is Skeleton.Color && radii == null && colors.isSingleColor() && type != Skeleton.Color.Type.TEXT
