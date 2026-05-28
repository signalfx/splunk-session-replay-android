package com.splunk.android.instrumentation.recording.wireframe.extension

import android.graphics.Point
import android.graphics.Rect
import com.splunk.android.common.utils.Colors
import com.splunk.android.common.utils.extensions.toArgbHexString
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts [Wireframe] to JSON that is expected by BE.
 */
@Suppress("unused")
fun Wireframe.toJSONObject(): JSONObject {
    return JSONObject()
        .put("version", version)
        .put("frames", frames.toJSONArray { it.toJSONObject() })
}

/**
 * Converts [Wireframe.Frame] to JSON that is expected by BE.
 */
fun Wireframe.Frame.toJSONObject(): JSONObject {
    return JSONObject()
        .put("scenes", scenes.toJSONArray { it.toJSONObject() })
}

/**
 * Converts [Wireframe.Frame.Scene] to JSON that is expected by BE.
 */
fun Wireframe.Frame.Scene.toJSONObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("time", time)
        .put("rect", rect.toJSONObject())
        .put("orientation", orientation?.toPayload())
        .put("type", type.toPayload())
        .put("windows", windows.toJSONArray { it.toJSONObject() })
}

private fun Wireframe.Frame.Scene.Window.toJSONObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("rect", rect.toJSONObject())
        .put("skeletons", skeletons?.toJSONArray { it.toJSONObject() })
        .put("subviews", subviews?.toJSONArray { it.toJSONObject() })
}

private fun WireframeView.toJSONObject(): JSONObject {
    return JSONObject()
        .put("id", id)
        .put("name", name)
        .put("typename", typename)
        .put("rect", rect.toJSONObject())
        .put("type", type?.toPayload())
        .put("focus", hasFocus.takeIf { it })
        .put("offset", offset?.toJSONObject())
        .put("alpha", alpha.takeIf { it != 1f })
        .put("skeletons", skeletons?.toJSONArray { it.toJSONObject() })
        .put("foregroundSkeletons", foregroundSkeletons?.toJSONArray { it.toJSONObject() })
        .put("subviews", subviews?.toJSONArray { it.toJSONObject() })
}

private fun Wireframe.Frame.Scene.Window.View.Skeleton.toJSONObject(): JSONObject {
    return when (this) {
        is Wireframe.Frame.Scene.Window.View.Skeleton.Color ->
            JSONObject()
                .put("type", "color")
                .put(
                    "color", JSONObject()
                        .put("rect", rect.toJSONObject())
                        .put("clipRect", clipRect?.toJSONObject())
                        .put("type", type.toPayload())
                        .put("colors", colors.toJSONArray())
                        .put("radius", radii?.takeIf { it.isAllSame() && it.topLeft != 0 }?.topLeft)
                        .put("radii", radii?.takeIf { !it.isAllSame() }?.toJSONArray())
                        .put("flags", flags?.toJSONObject())
                )
        is Wireframe.Frame.Scene.Window.View.Skeleton.Text ->
            JSONObject()
                .put("type", "text")
                .put(
                    "text", JSONObject()
                        .put("rect", rect.toJSONObject())
                        .put("clipRect", clipRect?.toJSONObject())
                        .put("text", text)
                        .put("color", color.toArgbHexString()) // FIXME alpha is not needed
                        .put("size", size)
                        .put("letterSpacing", letterSpacing.takeIf { it != 0f })
                        .put(
                            "font", JSONObject()
                                .put("familyName", font.familyName)
                                .put("isItalic", font.isItalic.takeIf { it })
                                .put("weight", font.weight.takeIf { it != 400 })
                        )
                )
    }
}

private fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Radii.toJSONArray(): JSONArray {
    return JSONArray()
        .put(topLeft)
        .put(topRight)
        .put(bottomRight)
        .put(bottomLeft)
}

private fun Colors.toJSONArray(): JSONArray {
    val columns = JSONArray()

    for (x in 0 until width) {
        val row = JSONArray()
        columns.put(row)

        for (y in 0 until height)
            row.put(get(x, y).toArgbHexString())
    }

    return columns
}

private fun <T> List<T>.toJSONArray(mapper: (T) -> JSONObject): JSONArray? {
    return takeIf { it.isNotEmpty() }?.map(mapper)?.let { JSONArray(it) }
}

private fun Rect.toJSONObject(): JSONObject {
    return JSONObject()
        .put("x", left)
        .put("y", top)
        .put("w", width())
        .put("h", height())
}

private fun Point.toJSONObject(): JSONObject {
    return JSONObject()
        .put("x", x)
        .put("y", y)
}

private fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.toPayload(): String? {
    return when (this) {
        Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.GENERAL -> null
        Wireframe.Frame.Scene.Window.View.Skeleton.Color.Type.TEXT -> "text"
    }
}

private fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow.toPayload(): String {
    return when (this) {
        Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow.LIGHT -> "light"
        Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.Shadow.DARK -> "dark"
    }
}

private fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Flags.toJSONObject(): JSONObject {
    return JSONObject()
        .put("shadow", shadow?.toPayload())
}

private fun Wireframe.Frame.Scene.Orientation.toPayload(): Int {
    return when (this) {
        Wireframe.Frame.Scene.Orientation.PORTRAIT -> 0
        Wireframe.Frame.Scene.Orientation.PORTRAIT_REVERSED -> 1
        Wireframe.Frame.Scene.Orientation.LANDSCAPE -> 2
        Wireframe.Frame.Scene.Orientation.LANDSCAPE_REVERSED -> 3
    }
}

private fun Wireframe.Frame.Scene.Type.toPayload(): Int {
    return when (this) {
        Wireframe.Frame.Scene.Type.DEVICE -> 0
        Wireframe.Frame.Scene.Type.MIRRORED -> 1
        Wireframe.Frame.Scene.Type.CONNECTED -> 2
    }
}

private fun Wireframe.Frame.Scene.Window.View.Type.toPayload(): Int {
    return when (this) {
        Wireframe.Frame.Scene.Window.View.Type.TEXT -> 1
        Wireframe.Frame.Scene.Window.View.Type.IMAGE -> 2
        Wireframe.Frame.Scene.Window.View.Type.AREA -> 3
        Wireframe.Frame.Scene.Window.View.Type.DIMMING -> 4
        Wireframe.Frame.Scene.Window.View.Type.VISUAL_EFFECT -> 5
        Wireframe.Frame.Scene.Window.View.Type.WEB_VIEW -> 6
        Wireframe.Frame.Scene.Window.View.Type.MAP -> 7
        Wireframe.Frame.Scene.Window.View.Type.TAP_BAR -> 8
        Wireframe.Frame.Scene.Window.View.Type.POPOVER -> 9
        Wireframe.Frame.Scene.Window.View.Type.DATE_PICKER -> 10
        Wireframe.Frame.Scene.Window.View.Type.TIME_PICKER -> 11
        Wireframe.Frame.Scene.Window.View.Type.PROGRESS -> 12
        Wireframe.Frame.Scene.Window.View.Type.SPINNING_WHEEL -> 13
        Wireframe.Frame.Scene.Window.View.Type.VIDEO -> 14
        Wireframe.Frame.Scene.Window.View.Type.SURFACE -> 15
        Wireframe.Frame.Scene.Window.View.Type.BUTTON -> 16
        Wireframe.Frame.Scene.Window.View.Type.SPINNER -> 17
        Wireframe.Frame.Scene.Window.View.Type.AD -> 18
        Wireframe.Frame.Scene.Window.View.Type.CHIP -> 19
    }
}
