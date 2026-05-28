/*
Copyright 2026 Splunk Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.splunk.android.bridge.model

import android.graphics.Point
import android.graphics.Rect
import com.splunk.android.common.utils.Colors

/**
 * Wireframe description. [View.rect] and [View.Skeleton.rect] must be different instances!
 */
class BridgeWireframe(
    val root: View,
    val width: Int,
    val height: Int
) {

    class View(
        val id: String,
        val name: String?,
        val rect: Rect,
        val type: Type?,
        val typename: String,
        val hasFocus: Boolean?,
        val offset: Point?,
        val alpha: Float?,
        val isSensitive: Boolean?,
        val skeletons: List<Skeleton>?,
        val foregroundSkeletons: List<Skeleton>?,
        val subviews: List<View>?
    ) {

        enum class Type {
            TEXT, IMAGE, AREA, DIMMING, VISUAL_EFFECT, WEB_VIEW, MAP, TAP_BAR, POPOVER, DATE_PICKER, TIME_PICKER, PROGRESS, SPINNING_WHEEL, VIDEO, SURFACE, BUTTON, SPINNER, AD, CHIP
        }

        sealed interface Skeleton {

            val rect: Rect
            val clipRect: Rect?

            data class Color(
                override val rect: Rect,
                override val clipRect: Rect?,
                val type: Type,
                val colors: Colors,
                val radii: Radii?,
                val flags: Flags?,
                val isOpaque: Boolean
            ) : Skeleton {

                enum class Type {
                    GENERAL, TEXT
                }

                data class Flags(
                    val shadow: Shadow?
                ) {

                    enum class Shadow {
                        LIGHT, DARK
                    }
                }

                data class Radii(
                    val topLeft: Int,
                    val topRight: Int,
                    val bottomRight: Int,
                    val bottomLeft: Int
                ) {

                    constructor(radius: Int) : this(radius, radius, radius, radius)
                }
            }

            data class Text(
                override val rect: Rect,
                override val clipRect: Rect?,
                val text: CharSequence,
                val color: Int,
                val size: Float,
                val letterSpacing: Float,
                val font: Font
            ) : Skeleton {

                data class Font(
                    val familyName: String,
                    val isItalic: Boolean,
                    val weight: Int
                )
            }
        }
    }
}
