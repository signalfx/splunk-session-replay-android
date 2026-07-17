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

package com.splunk.android.instrumentation.recording.wireframe.model

import android.graphics.Point
import android.graphics.Rect
import com.splunk.rum.common.utils.Colors
import com.splunk.rum.common.utils.Lock

/**
 * Structure that simplify content of the screen.
 * See WireframeExt extensions for find operations, JSON and ByteArray conversions, ...
 */
data class Wireframe internal constructor(
    val frames: List<Frame>,
    val version: String = "1.1.0"
) {

    /**
     * Single screen frame.
     */
    data class Frame internal constructor(
        val scenes: List<Scene>
    ) {

        /**
         * Single scene data.
         */
        data class Scene internal constructor(
            val id: String,
            val time: Long,
            val rect: Rect,
            val orientation: Orientation?,
            val type: Type,
            val windows: List<Window>
        ) {

            /**
             * Scene orientation. This is not the screen orientation. For example, in split screen, screen orientation can be landscape and scene orientation portrait.
             */
            enum class Orientation {
                PORTRAIT, PORTRAIT_REVERSED, LANDSCAPE, LANDSCAPE_REVERSED
            }

            /**
             * Scene type.
             */
            enum class Type {
                DEVICE, MIRRORED, CONNECTED
            }

            /**
             * Represents Activity, Dialog, PopupWindow, ...
             */
            data class Window internal constructor(
                val id: String,
                val rect: Rect,
                val skeletons: List<View.Skeleton>?,
                val subviews: List<View>?,
                internal val identity: String
            ) {

                /**
                 * Represents Android's View.
                 */
                data class View(
                    val id: String,
                    val name: String?,
                    val rect: Rect,
                    val type: Type?,
                    val typename: String,
                    val hasFocus: Boolean,
                    val offset: Point?,
                    val alpha: Float,
                    val skeletons: List<Skeleton>?,
                    val foregroundSkeletons: List<Skeleton>?,
                    val subviews: MutableList<View>?,
                    val identity: String,
                    internal val isDrawDeterministic: Boolean,
                    val isSensitive: Boolean?,
                    internal val subviewsLock: Lock?
                ) {

                    /**
                     * View type that can be used to categorize the view.
                     */
                    enum class Type {
                        TEXT, IMAGE, AREA, DIMMING, VISUAL_EFFECT, WEB_VIEW, MAP, TAP_BAR, POPOVER, DATE_PICKER, TIME_PICKER, PROGRESS, SPINNING_WHEEL, VIDEO, SURFACE, BUTTON, SPINNER, AD, CHIP
                    }

                    /**
                     * Object to be drawn.
                     */
                    sealed interface Skeleton {

                        val rect: Rect
                        val clipRect: Rect?

                        data class Color internal constructor(
                            override val rect: Rect,
                            override val clipRect: Rect?,
                            val type: Type,
                            val colors: Colors,
                            val radii: Radii?,
                            val flags: Flags?,
                            internal val isOpaque: Boolean
                        ) : Skeleton {

                            enum class Type {
                                GENERAL, TEXT
                            }

                            /**
                             * Various View properties.
                             */
                            data class Flags internal constructor(
                                val shadow: Shadow?
                            ) {

                                /**
                                 * The nature of the shadow cast.
                                 */
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

                                fun isAllSame(): Boolean {
                                    return topLeft == topRight && topRight == bottomRight && bottomRight == bottomLeft
                                }
                            }
                        }

                        data class Text internal constructor(
                            override val rect: Rect,
                            override val clipRect: Rect?,
                            val text: CharSequence,
                            val color: Int,
                            val size: Float,
                            val letterSpacing: Float,
                            val font: Font
                        ) : Skeleton {

                            data class Font internal constructor(
                                val familyName: String,
                                val isItalic: Boolean,
                                val weight: Int
                            )
                        }
                    }
                }
            }
        }

        companion object
    }

    companion object
}
