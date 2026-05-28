package com.splunk.android.instrumentation.recording.wireframe.extension

import com.splunk.android.bridge.model.BridgeWireframe
import com.splunk.android.common.utils.extensions.identity
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal fun BridgeWireframe.View.toWireframeView(): WireframeView {
    return toWireframeView("")
}

private fun BridgeWireframe.View.toWireframeView(path: String): WireframeView {
    return WireframeView(
        id = id,
        name = name,
        rect = rect,
        type = type?.toWireframeViewType(),
        typename = typename,
        hasFocus = hasFocus ?: false,
        offset = offset,
        alpha = alpha ?: 1f,
        skeletons = skeletons?.map { it.toViewSkeleton() },
        foregroundSkeletons = foregroundSkeletons?.map { it.toViewSkeleton() },
        subviews = subviews?.map { it.toWireframeView(identity) } as? MutableList,
        identity = "$path/$id",
        isDrawDeterministic = true,
        isSensitive = isSensitive ?: false,
        subviewsLock = null
    )
}

private fun BridgeWireframe.View.Type.toWireframeViewType(): Wireframe.Frame.Scene.Window.View.Type {
    return when (this) {
        BridgeWireframe.View.Type.TEXT -> Wireframe.Frame.Scene.Window.View.Type.TEXT
        BridgeWireframe.View.Type.IMAGE -> Wireframe.Frame.Scene.Window.View.Type.IMAGE
        BridgeWireframe.View.Type.AREA -> Wireframe.Frame.Scene.Window.View.Type.AREA
        BridgeWireframe.View.Type.DIMMING -> Wireframe.Frame.Scene.Window.View.Type.DIMMING
        BridgeWireframe.View.Type.VISUAL_EFFECT -> Wireframe.Frame.Scene.Window.View.Type.VISUAL_EFFECT
        BridgeWireframe.View.Type.WEB_VIEW -> Wireframe.Frame.Scene.Window.View.Type.WEB_VIEW
        BridgeWireframe.View.Type.MAP -> Wireframe.Frame.Scene.Window.View.Type.MAP
        BridgeWireframe.View.Type.TAP_BAR -> Wireframe.Frame.Scene.Window.View.Type.TAP_BAR
        BridgeWireframe.View.Type.POPOVER -> Wireframe.Frame.Scene.Window.View.Type.POPOVER
        BridgeWireframe.View.Type.DATE_PICKER -> Wireframe.Frame.Scene.Window.View.Type.DATE_PICKER
        BridgeWireframe.View.Type.TIME_PICKER -> Wireframe.Frame.Scene.Window.View.Type.TIME_PICKER
        BridgeWireframe.View.Type.PROGRESS -> Wireframe.Frame.Scene.Window.View.Type.PROGRESS
        BridgeWireframe.View.Type.SPINNING_WHEEL -> Wireframe.Frame.Scene.Window.View.Type.SPINNING_WHEEL
        BridgeWireframe.View.Type.VIDEO -> Wireframe.Frame.Scene.Window.View.Type.VIDEO
        BridgeWireframe.View.Type.SURFACE -> Wireframe.Frame.Scene.Window.View.Type.SURFACE
        BridgeWireframe.View.Type.BUTTON -> Wireframe.Frame.Scene.Window.View.Type.BUTTON
        BridgeWireframe.View.Type.SPINNER -> Wireframe.Frame.Scene.Window.View.Type.SPINNER
        BridgeWireframe.View.Type.AD -> Wireframe.Frame.Scene.Window.View.Type.AD
        BridgeWireframe.View.Type.CHIP -> Wireframe.Frame.Scene.Window.View.Type.CHIP
    }
}

private fun BridgeWireframe.View.Skeleton.toViewSkeleton(): Wireframe.Frame.Scene.Window.View.Skeleton {
    return when (this) {
        is BridgeWireframe.View.Skeleton.Color ->
            Wireframe.Frame.Scene.Window.View.Skeleton.Color(rect, clipRect, type.toViewSkeletonType(), colors, radii = radii?.toViewSkeletonRadii(), flags?.toViewSkeletonFlags(), isOpaque)
        is BridgeWireframe.View.Skeleton.Text ->
            Wireframe.Frame.Scene.Window.View.Skeleton.Text(rect, clipRect, text, color, size, letterSpacing, font.toViewSkeletonTextFont())
    }
}

private fun BridgeWireframe.View.Skeleton.Color.Type.toViewSkeletonType(): Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type {
    return when (this) {
        BridgeWireframe.View.Skeleton.Color.Type.GENERAL -> Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.GENERAL
        BridgeWireframe.View.Skeleton.Color.Type.TEXT -> Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.TEXT
    }
}

private fun BridgeWireframe.View.Skeleton.Color.Flags.toViewSkeletonFlags(): Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags {
    return Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags(shadow?.toViewSkeletonSlagsShadow())
}

private fun BridgeWireframe.View.Skeleton.Color.Flags.Shadow.toViewSkeletonSlagsShadow(): Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow {
    return when (this) {
        BridgeWireframe.View.Skeleton.Color.Flags.Shadow.LIGHT -> Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow.LIGHT
        BridgeWireframe.View.Skeleton.Color.Flags.Shadow.DARK -> Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow.DARK
    }
}

private fun BridgeWireframe.View.Skeleton.Text.Font.toViewSkeletonTextFont(): Wireframe.Frame.Scene.Window.View.Skeleton.Text.Font {
    return Wireframe.Frame.Scene.Window.View.Skeleton.Text.Font(familyName, isItalic, weight)
}

private fun BridgeWireframe.View.Skeleton.Color.Radii.toViewSkeletonRadii(): Wireframe.Frame.Scene.Window.View.Skeleton.Color.Radii {
    return Wireframe.Frame.Scene.Window.View.Skeleton.Color.Radii(topLeft, topRight, bottomRight, bottomLeft)
}
