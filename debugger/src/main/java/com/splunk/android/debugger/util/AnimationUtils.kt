package com.splunk.android.debugger.util

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewAnimationUtils
import com.splunk.android.common.utils.extensions.doOnLayout
import com.splunk.android.debugger.extension.addListener
import com.splunk.android.debugger.extension.getRectOnScreen
import com.splunk.android.debugger.extension.withDisabledAnimations
import kotlin.math.max
import kotlin.math.sqrt

internal object AnimationUtils {

    private const val DEFAULT_DURATION = 300L

    fun reveal(revealView: View, sourceView: View, show: Boolean, duration: Long = DEFAULT_DURATION) {
        if (revealView.width == 0) {
            revealView.withDisabledAnimations {
                revealView.visibility = View.VISIBLE
                revealView.alpha = 0f
            }

            revealView.doOnLayout { reveal(revealView, sourceView, show, duration) }
            return
        }

        val sourceRect = sourceView.getRectOnScreen()
        val revealRect = revealView.getRectOnScreen()

        val sourceWidthHalf = sourceRect.width() / 2
        val sourceHeightHalf = sourceRect.height() / 2

        val centerX = sourceRect.left - revealRect.left + sourceWidthHalf
        val centerY = sourceRect.top - revealRect.top + sourceHeightHalf

        val sizeWidth = max(revealRect.width() - centerX, centerX).toFloat()
        val sizeHeight = max(revealRect.height() - centerY, centerY).toFloat()

        val diagonal = sqrt(sizeWidth * sizeWidth + sizeHeight * sizeHeight)

        val startRadius: Float
        val endRadius: Float
        val beginAlpha = revealView.alpha
        val endAlpha: Float

        if (show) {
            startRadius = 0f
            endRadius = diagonal
            endAlpha = 1f
        } else {
            startRadius = diagonal
            endRadius = 0f
            endAlpha = 0f
        }

        revealView.withDisabledAnimations {
            revealView.visibility = View.VISIBLE

            if (show)
                revealView.alpha = 0f
        }

        val revealAnimator = ViewAnimationUtils.createCircularReveal(revealView, centerX, centerY, startRadius, endRadius)
        revealAnimator.addListener(
            onEnd = {
                if (!show)
                    revealView.withDisabledAnimations {
                        revealView.visibility = View.GONE
                    }
            }
        )
        revealAnimator.duration = duration
        revealAnimator.start()

        val alphaAnimator = ObjectAnimator.ofFloat(revealView, "alpha", beginAlpha, endAlpha)
        alphaAnimator.duration = duration
        alphaAnimator.start()
    }
}
