package com.splunk.android.instrumentation.recording.wireframe.canvas.compose

import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.os.Build
import android.widget.EdgeEffect
import androidx.annotation.RequiresApi
import com.splunk.android.instrumentation.recording.wireframe.canvas.SkeletonCanvas

@RequiresApi(Build.VERSION_CODES.S)
internal class ComposeEdgeEffect(
    context: Context,
    private val effect: EdgeEffect
) : EdgeEffect(context) {

    override fun setSize(width: Int, height: Int) {
        effect.setSize(width, height)
    }

    override fun isFinished(): Boolean {
        return !isEffectGloballyEnabled || effect.isFinished
    }

    override fun finish() {
        if (isEffectGloballyEnabled)
            effect.finish()
    }

    override fun onPull(deltaDistance: Float) {
        if (isEffectGloballyEnabled)
            effect.onPull(deltaDistance)
    }

    override fun onPull(deltaDistance: Float, displacement: Float) {
        if (isEffectGloballyEnabled)
            effect.onPull(deltaDistance, displacement)
    }

    override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
        return if (isEffectGloballyEnabled) effect.onPullDistance(deltaDistance, displacement) else 0f
    }

    override fun getDistance(): Float {
        return if (isEffectGloballyEnabled) effect.distance else 0f
    }

    override fun onRelease() {
        if (isEffectGloballyEnabled)
            effect.onRelease()
    }

    override fun onAbsorb(velocity: Int) {
        if (isEffectGloballyEnabled)
            effect.onAbsorb(velocity)
    }

    override fun setColor(color: Int) {
        effect.color = color
    }

    override fun setBlendMode(blendmode: BlendMode?) {
        effect.blendMode = blendmode
    }

    override fun getColor(): Int {
        return effect.color
    }

    override fun getBlendMode(): BlendMode? {
        return effect.blendMode
    }

    override fun draw(canvas: Canvas): Boolean {
        if (canvas is SkeletonCanvas)
            return !isFinished

        return effect.draw(canvas)
    }

    override fun getMaxHeight(): Int {
        return effect.maxHeight
    }

    companion object {
        var isEffectGloballyEnabled = true
    }
}
