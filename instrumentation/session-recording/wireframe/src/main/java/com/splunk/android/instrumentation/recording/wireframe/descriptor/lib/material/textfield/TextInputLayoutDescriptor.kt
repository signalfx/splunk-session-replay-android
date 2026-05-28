package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textfield

import android.annotation.SuppressLint
import android.text.Layout
import android.view.View
import com.google.android.material.internal.CollapsingTextHelper
import com.google.android.material.textfield.TextInputLayout
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.common.utils.reflector.Reflector
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.forEachSkeleton
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.stats.StatsCollector

/* MARK
 *  - Compatible with com.google.android.material:material:1.2.0
 *  - Requires Proguard rules
 */
internal open class TextInputLayoutDescriptor : LinearLayoutDescriptor() {

    private val reflector = Reflector(4, 0, 0)

    override val intendedClass: Class<*>? = "com.google.android.material.textfield.TextInputLayout".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        super.getForegroundSkeletons(view, isSensitive, result)

        if (view !is TextInputLayout)
            return

        getHintSkeletons(view, isSensitive, result)
    }

    @SuppressLint("RestrictedApi")
    private fun getHintSkeletons(view: TextInputLayout, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {
        try {
            StatsCollector.measureTextsTime {
                if (!view.isHintEnabledSafe)
                    return

                reflector.reflect {
                    val collapsingTextHelper = view.get<CollapsingTextHelper>("collapsingTextHelper") ?: return@reflect
                    val layout = collapsingTextHelper.get<Layout>("textLayout") ?: return@reflect
                    val xOffset = collapsingTextHelper.get<Float>("currentDrawX")?.toInt() ?: 0
                    val yOffset = collapsingTextHelper.get<Float>("currentDrawY")?.toInt() ?: 0

                    layout.forEachSkeleton(!isSensitive, Int.MAX_VALUE) {
                        it.rect.offset(xOffset, yOffset)
                        result += it
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e1(TAG, "getHintSkeletons", e)
        }
    }

    private val TextInputLayout.isHintEnabledSafe: Boolean
        get() = try {
            isHintEnabled
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "getIsHintEnabledSafe", e)
            true
        }

    private companion object {
        const val TAG = "TextInputLayoutDescriptor"
    }
}
