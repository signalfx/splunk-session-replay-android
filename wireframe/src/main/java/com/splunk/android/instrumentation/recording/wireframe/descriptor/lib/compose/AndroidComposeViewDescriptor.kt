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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.EdgeEffect
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collection.MutableVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.MeasurePolicy
import com.splunk.android.instrumentation.recording.wireframe.canvas.compose.ComposeCanvas
import com.splunk.android.instrumentation.recording.wireframe.canvas.compose.ComposeEdgeEffect
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.WireframeView
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.util.ComposeInfo
import com.splunk.android.instrumentation.recording.wireframe.util.ComposeTextFieldSensitivity
import com.splunk.android.instrumentation.recording.wireframe.util.FragmentConsumer
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_10
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_2
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_3
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_4
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_7
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_8
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_9
import com.splunk.android.instrumentation.recording.wireframe.util.ViewConsumer
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.MutableListObserver
import com.splunk.rum.common.utils.extensions.findField
import com.splunk.rum.common.utils.extensions.forEachFast
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.identity
import com.splunk.rum.common.utils.extensions.invoke
import com.splunk.rum.common.utils.extensions.set
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.rum.common.utils.reflector.Reflector

/* MARK
 *  - The following Google samples from 2022.02.07 are OK
 *      - Jetcaster, Jetnews, Owl
 * MARK 3rd party libraries can contain extension functions and their existence can change name of anonymous classes.
 *  Remove or add 'com.google.accompanist:accompanist-permissions:0.28.0' dependency in app module to test this behaviour.
 */

// MARK Requires Proguard rules

/* FIXME
 *  - Google samples from 2024.09.02
 *      - Jetsnack
 *          - Missing text in filter options in Dashboard
 *          - Missing debug window when tap on filter button in Dashboard
 *          - Missing content on dashboard
 *      - Jetchat
 *          - Profile images
 *      - Jetsurvey
 *          - Covered debug window by calendar at 4th page
 *      - Crane
 *          - Missing splash image (saveLayer)
 *      - JetNews
 *          - Crash java.lang.IndexOutOfBoundsException: setSpan (76 ... 96) ends beyond length 0
 *  - Compose 1.8.3
 *      - Missing icons in video
 *  - Compose 1.9.4
 *      - App > Surface - Different background color.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
internal open class AndroidComposeViewDescriptor : ViewGroupDescriptor() {

    private val workaroundEdgeEffectReflector = Reflector(4, 0, 0)
    private var wrapOverscrollEffectsReflector: Reflector? = null

    private val findSharedBoundsReflector = Reflector(3, 0, 1)
    private val sharedBoundsNodeReflector = Reflector(3, 2, 0)

    private val sharedBoundsNodes = ArrayList<Any>()
    private val nodeBackups = ArrayList<NodeBackup>()

    private val composeCanvas = ComposeCanvas()

    override val intendedClass: Class<*>? = "androidx.compose.ui.platform.AndroidComposeView".toClass()

    override fun getExtractionMode(view: View): ExtractionMode {
        return ExtractionMode.TRAVERSE
    }

    override fun describe(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?, viewConsumer: ViewConsumer, fragmentConsumer: FragmentConsumer): Wireframe.Frame.Scene.Window.View {
        var description = super.describe(view, viewRect, clipRect, parentScaleX, parentScaleY, isParentSensitive, viewConsumer, fragmentConsumer)

        val dirtyLayersDepot = lazy { obtainDirtyLayersDepot(view) }
        val rootNode = getRootNode(view)

        ComposeTextFieldSensitivity.makeSensitive(rootNode)

        drawWithRenderNodeWorkaround(dirtyLayersDepot) {
            drawWithEdgeEffectWorkaround(view, dirtyLayersDepot, rootNode) {
                drawWithSharedBoundsNodeWorkaround(rootNode) {
                    composeCanvas.beginDraw()
                    extractSkeletonsCanvas(composeCanvas, view, viewRect, clipRect, parentScaleX, parentScaleY, description.isSensitive != false, null)
                    composeCanvas.endDraw()
                }
            }
        }

        description = moveViewsIntoRoot(description)
        description = filterUnknownViews(description, composeCanvas.elements) // FIXME v1.7.8 App > Video - Elements are removed here because Compose doesn't draw anything (breakpoint description.getSkeletonCountRecursively() == 0)
        description = updateViewsProperties(description, composeCanvas.elements)
        description = mergeViewsAndElements(description, composeCanvas.elements)

        composeCanvas.elements.clear()

        return description
    }

    private inline fun drawWithRenderNodeWorkaround(dirtyLayersDepot: Lazy<DirtyLayersDepot?>, crossinline draw: () -> Unit) {
        val dirtyLayersDepot = if (ComposeInfo.version < VERSION_1_8) dirtyLayersDepot.value else null
        dirtyLayersDepot?.begin()

        draw()

        if (dirtyLayersDepot?.dirtyLayersCache?.isEmpty() == false)
            restoreDirtyLayers(dirtyLayersDepot)

        dirtyLayersDepot?.end()
    }

    /* MARK Bug in Android SDK in EdgeEffect class on line 606
     *  Internal state is reset when EdgeEffect is drawn into out ComposeCanvas on Android 12+. The implementation does not assume draw with non RecordingCanvas
     *  and edgeEffectBehavior = TYPE_STRETCH.
     *  The issue can be reproduced on Android 12+ in ListComposeActivity by swiping down to bring overscroll effect.
     *
     * MARK Because of Google's fix in v1.9.0. In AndroidOverscroll.android.kt on line 217 is check for HW acceleration,
     *  the edge effect is finished if the Canvas is not HW accelerated.
     */
    private inline fun drawWithEdgeEffectWorkaround(view: View, dirtyLayersDepot: Lazy<DirtyLayersDepot?>, rootNode: Any?, crossinline draw: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dirtyLayersDepot = if (ComposeInfo.version < VERSION_1_8) dirtyLayersDepot.value else null

            if (dirtyLayersDepot?.isNotEmpty() ?: !isDirtyLayersEmpty(view))
                workaroundEdgeEffect(view.context, rootNode)

            if (ComposeInfo.version >= VERSION_1_7)
                ComposeEdgeEffect.isEffectGloballyEnabled = false
        }

        draw()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ComposeInfo.version >= VERSION_1_7)
            ComposeEdgeEffect.isEffectGloballyEnabled = true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun workaroundEdgeEffect(context: Context, layoutNode: Any?) {
        layoutNode ?: return

        workaroundEdgeEffectReflector.reflect {
            try {
                when {
                    ComposeInfo.version >= VERSION_1_10 -> { // FIXME 1.10 is not ideal
                        layoutNode.get<Modifier>("_modifier")?.all {
                            if (it::class.java == SCROLLABLE_AREA_ELEMENT_CLASS) {
                                val overscrollEffect = it.get<Any>("overscrollEffect") ?: return@all true
                                val edgeEffectWrapper = overscrollEffect.get<Any>("edgeEffectWrapper") ?: return@all true

                                wrapOverscrollEffects(context, edgeEffectWrapper)
                            }

                            true
                        }
                    }
                    ComposeInfo.version >= VERSION_1_9 -> { // FIXME 1.9 is not ideal
                        layoutNode.get<Modifier>("_modifier")?.all {
                            if (it::class.java == SCROLLING_CONTAINER_ELEMENT_CLASS) {
                                val overscrollEffect = it.get<Any>("overscrollEffect") ?: return@all true
                                val edgeEffectWrapper = overscrollEffect.get<Any>("edgeEffectWrapper") ?: return@all true

                                wrapOverscrollEffects(context, edgeEffectWrapper)
                            }

                            true
                        }
                    }
                    // FIXME 1.8 is not ideal
                    ComposeInfo.version >= VERSION_1_7 -> {
                        layoutNode.get<Modifier>("_modifier")?.all {
                            if (it::class.java == DRAW_STRETCH_OVERSCROLL_MODIFIER) {
                                val edgeEffectWrapper = it.get<Any>("edgeEffectWrapper") ?: return@all true

                                wrapOverscrollEffects(context, edgeEffectWrapper)
                            }

                            true
                        }
                    }
                    ComposeInfo.version >= VERSION_1_4 -> {
                        layoutNode.get<Modifier>("modifier")?.all {
                            if (it::class.java == DRAW_OVERSCROLL_MODIFIER) {
                                val overscrollEffect = it.get<Any>("overscrollEffect") ?: return@all true

                                wrapOverscrollEffects(context, overscrollEffect)
                            }

                            true
                        }
                    }
                    ComposeInfo.version >= VERSION_1_2 -> {
                        val overscrollEffect = layoutNode
                            .get<MeasurePolicy>("measurePolicy")
                            ?.get<Any>("\$block")
                            ?.get<Any>("\$measurePolicy")
                            ?.get<Any>("\$overscrollEffect")

                        if (overscrollEffect != null && overscrollEffect::class.java == EDGE_EFFECT_WORKAROUND_OVERSCROLL_EFFECT)
                            wrapOverscrollEffects(context, overscrollEffect)
                    }
                }

                Unit
            } catch (e: NoSuchFieldException) {
                Logger.e1(TAG, "workaroundEdgeEffect", e)
            }
        }

        val children: MutableVector<Any>? = try {
            when {
                ComposeInfo.version >= VERSION_1_3 ->
                    layoutNode.get<Any>("_foldedChildren")?.get("vector")
                else ->
                    layoutNode.get("_foldedChildren")
            }
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "workaroundEdgeEffect", e)
            null
        }

        if (children != null)
            for (i in children.indices)
                workaroundEdgeEffect(context, children[i])
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun wrapOverscrollEffects(context: Context, overscrollEffect: Any) {
        when {
            ComposeInfo.version >= VERSION_1_7 -> { // This implementation breaks EdgeEffect lazy creation, but there was not found any better solution.
                val reflector = wrapOverscrollEffectsReflector ?: Reflector(0, 8, 8).also { wrapOverscrollEffectsReflector = it }

                reflector.reflect {
                    val wrapEdgeEffect: (fieldName: String, methodName: String) -> Unit = { fieldName, methodName ->
                        val edgeEffect = overscrollEffect.invoke<EdgeEffect>(methodName)

                        if (edgeEffect != null && edgeEffect !is ComposeEdgeEffect)
                            overscrollEffect.set(fieldName, ComposeEdgeEffect(context, edgeEffect))
                    }

                    wrapEdgeEffect("leftEffect", "getOrCreateLeftEffect")
                    wrapEdgeEffect("topEffect", "getOrCreateTopEffect")
                    wrapEdgeEffect("rightEffect", "getOrCreateRightEffect")
                    wrapEdgeEffect("bottomEffect", "getOrCreateBottomEffect")
                    wrapEdgeEffect("leftEffectNegation", "getOrCreateLeftEffectNegation")
                    wrapEdgeEffect("topEffectNegation", "getOrCreateTopEffectNegation")
                    wrapEdgeEffect("rightEffectNegation", "getOrCreateRightEffectNegation")
                    wrapEdgeEffect("bottomEffectNegation", "getOrCreateBottomEffectNegation")
                }
            }
            ComposeInfo.version >= VERSION_1_2 -> {
                val reflector = wrapOverscrollEffectsReflector ?: Reflector(4, 4, 0).also { wrapOverscrollEffectsReflector = it }

                reflector.reflect {
                    val wrapEdgeEffect: (fieldName: String) -> Unit = {
                        val edgeEffect = overscrollEffect.get<EdgeEffect>(it)

                        if (edgeEffect != null && edgeEffect !is ComposeEdgeEffect)
                            overscrollEffect.set(it, ComposeEdgeEffect(context, edgeEffect))
                    }

                    wrapEdgeEffect("leftEffect")
                    wrapEdgeEffect("topEffect")
                    wrapEdgeEffect("rightEffect")
                    wrapEdgeEffect("bottomEffect")
                }
            }
        }
    }

    /* MARK Compose is drawing SharedBoundsNode into RecordingCanvas and RenderNode. Because of these classes are final, we are not able to catch
     *  draw calls that is used to construct Wireframe. The following code removes the SharedBoundsNodes from the Node chain that causes Compose UI
     *  is drawn into our ComposeCanvas.
     *
     * FIXME Layout blinks when click on element with Modifier.sharedBounds in SharedBoundsComposeActivity
     */
    private inline fun drawWithSharedBoundsNodeWorkaround(rootNode: Any?, crossinline draw: () -> Unit) {
        if (ComposeInfo.version == VERSION_1_7) {
            val rootNodeValue = rootNode ?: return

            findSharedBoundsNodes(rootNodeValue, sharedBoundsNodes)

            sharedBoundsNodeReflector.reflect {
                try {
                    sharedBoundsNodes.forEachFast { node ->
                        val coordinator = node.get<Any>("coordinator") ?: return@forEachFast
                        val wrapped = coordinator.get<Any>("wrapped") ?: return@forEachFast
                        val wrappedBy = coordinator.get<Any>("wrappedBy") ?: return@forEachFast

                        nodeBackups += NodeBackup(coordinator, wrapped, wrappedBy)

                        wrappedBy.set("wrapped", wrapped)
                        wrapped.set("wrappedBy", wrappedBy)
                    }
                } catch (e: NoSuchFieldException) {
                    Logger.e1(TAG, "drawWithSharedBoundsNodeWorkaround", e)
                }
            }

            draw()

            sharedBoundsNodeReflector.reflect {
                try {
                    nodeBackups.forEachFast { (coordinator, wrapped, wrappedBy) ->
                        wrappedBy.set("wrapped", coordinator)
                        wrapped.set("wrappedBy", coordinator)
                    }
                } catch (e: NoSuchFieldException) {
                    Logger.e1(TAG, "drawWithSharedBoundsNodeWorkaround()", e)
                }
            }

            sharedBoundsNodes.clear()
            nodeBackups.clear()
        } else
            draw()
    }

    private fun findSharedBoundsNodes(layoutNode: Any, nodes: MutableList<Any>) {
        val childrenList = findSharedBoundsReflector.reflect {
            try {
                val nodeChain = layoutNode.get<Any>("nodes") ?: return@reflect null
                var currentNode = nodeChain.get<Any>("head")

                while (currentNode != null) {
                    if (currentNode.javaClass == SHARED_BOUNDS_NODE_CLASS)
                        nodes += currentNode

                    currentNode = currentNode.get("child")
                }

                layoutNode.invoke<List<*>>("getChildren\$ui_release")
            } catch (e: NoSuchFieldException) {
                Logger.e1(TAG, "findSharedBoundsNodes()", e)
                null
            } catch (e: NoSuchMethodException) {
                Logger.e1(TAG, "findSharedBoundsNodes()", e)
                null
            }
        }

        childrenList?.forEachFast {
            if (it != null)
                findSharedBoundsNodes(it, nodes)
        }
    }

    /* MARK Bug in Jetpack Compose library in RenderNodeLayer class on line 274 makes Compose incompatible with rendering into two Canvases.
     *  Dirty layers (changes) are result of recomposition and they are consumed by the next Canvas. It means that another Canvas will not be updated.
     *  There are two cases that must be handled.
     *  - The case reproducible in MeasureRecompositionComposeActivity by waiting few seconds to layout enlargement. Layout is remeasured by AndroidComposeViewDescriptor and dirty layers are consumer by our ComposeCanvas.
     *  - The case reproducible in DrawRecompositionComposeActivity by clicking on check boxes. Due to our frame rate, the issue is not reproducible in every frame. Dirty layers from previous recomposition are consumed by our ComposeCanvas.
     *  The workaround is not needed in Compose v1.7+.
     */
    private fun obtainDirtyLayersDepot(view: View): DirtyLayersDepot? {
        val dirtyLayerField = try {
            view.javaClass.findField("dirtyLayers")
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "obtainDirtyLayersDepot", e)
            return null
        }

        var dirtyLayers = view.get<MutableList<Any>>(dirtyLayerField) ?: return null

        if (dirtyLayers !is DirtyLayersDepot) {
            dirtyLayers = DirtyLayersDepot(dirtyLayers)
            view.set(dirtyLayerField, dirtyLayers)
        }

        return dirtyLayers
    }

    private fun isDirtyLayersEmpty(view: View): Boolean {
        val dirtyLayerField = try {
            view.javaClass.findField("dirtyLayers")
        } catch (e: NoSuchFieldException) {
            Logger.e1(TAG, "isDirtyLayersNotEmpty", e)
            return true
        }

        val list = view.get<Any>(dirtyLayerField) ?: return true

        return when {
            list is List<*> ->
                list.isEmpty()
            list::class.java == MUTABLE_OBJECT_LIST_CLASS ->
                try {
                    list.invoke("none") ?: true
                } catch (_: NoSuchMethodException) {
                    Logger.e1(TAG, "isDirtyLayersEmpty() - Method not found'")
                    true
                }
            else -> {
                Logger.e1(TAG, "isDirtyLayersEmpty() - Unknown list implementation '${list::class.java.name}'")
                true
            }
        }
    }

    private fun restoreDirtyLayers(dirtyLayersDepot: DirtyLayersDepot) {
        dirtyLayersDepot.originalDirtyLayers.clear()

        for (i in dirtyLayersDepot.dirtyLayersCache.indices) {
            val layer = dirtyLayersDepot.dirtyLayersCache[i]
            dirtyLayersDepot.originalDirtyLayers += layer

            try {
                layer.set("isDirty", true)
            } catch (e: NoSuchFieldException) {
                Logger.e1(TAG, "restoreDirtyLayers", e)
            }
        }
    }

    @Suppress("RemoveExplicitTypeArguments")
    private fun getRootNode(view: View): Any? {
        return try {
            view.get<Any>("root") // Type must be here!
        } catch (e: Exception) {
            Logger.e1(TAG, "getRootNode", e)
            null
        }
    }

    // AndroidComposeView > AndroidViewsHandler > N of ViewFactoryHolder > View
    private fun moveViewsIntoRoot(androidComposeView: WireframeView): WireframeView {
        val androidViewsHandler = androidComposeView.subviews?.lastOrNull() ?: return androidComposeView
        val viewFactoryHolderList = androidViewsHandler.subviews ?: return androidComposeView

        val subviews = androidComposeView.subviews ?: ArrayList(viewFactoryHolderList.size)
        subviews.clear()

        for (viewFactoryHolder in viewFactoryHolderList)
            subviews += viewFactoryHolder.subviews?.firstOrNull() ?: continue

        return if (subviews !== androidComposeView.subviews)
            androidComposeView.copy(
                subviews = subviews
            )
        else
            androidComposeView
    }

    // AndroidComposeView > N of View
    private fun filterUnknownViews(androidComposeView: WireframeView, elements: List<ComposeCanvas.Element>): WireframeView {
        val subviews = androidComposeView.subviews ?: return androidComposeView
        var index = 0

        loop@ while (index != subviews.size) {
            val wireframeView = subviews[index]

            if (wireframeView.type == Wireframe.Frame.Scene.Window.View.Type.SURFACE) {
                index++
                continue
            }

            for (i in elements.indices) {
                val element = elements[i]

                if (element is ComposeCanvas.Element.View && element.view.identity == wireframeView.identity) {
                    index++
                    continue@loop
                }
            }

            subviews.removeAt(index)
        }

        return androidComposeView
    }

    // AndroidComposeView > N of View
    private fun updateViewsProperties(androidComposeView: WireframeView, elements: List<ComposeCanvas.Element>): WireframeView {
        val subviews = androidComposeView.subviews ?: return androidComposeView

        for (element in elements)
            if (element is ComposeCanvas.Element.View) {
                val modifier = element.modifier

                if (modifier.isSensitive == null && modifier.id == null)
                    continue

                val viewIdentity = element.view.identity
                val index = subviews.indexOfFirst { it.identity == viewIdentity }
                val view = subviews.getOrNull(index) ?: continue

                if (view.id == modifier.id && view.isSensitive == modifier.isSensitive)
                    continue

                subviews[index] = view.copy(
                    id = modifier.id ?: view.id,
                    isSensitive = modifier.isSensitive ?: view.isSensitive
                )
            }

        return androidComposeView
    }

    // AndroidComposeView > N of View
    private fun mergeViewsAndElements(androidComposeView: WireframeView, elements: List<ComposeCanvas.Element>): WireframeView {
        val subviews = androidComposeView.subviews ?: ArrayList(elements.size)
        var index = 0

        for (element in elements)
            when (element) {
                is ComposeCanvas.Element.Compose -> {
                    subviews.add(index++, element.view)
                }
                is ComposeCanvas.Element.View -> {
                    val identity = element.view.identity

                    if (index < subviews.size && subviews[index].identity == identity || subviews.any { it.identity == identity })
                        index++
                }
            }

        return if (subviews !== androidComposeView.subviews)
            androidComposeView.copy(
                subviews = subviews
            )
        else
            androidComposeView
    }

    private class DirtyLayersDepot(val originalDirtyLayers: MutableList<Any>) : MutableListObserver<Any>(originalDirtyLayers, Observer()) {

        private val localObserver: Observer
            get() = (observer as Observer)

        val dirtyLayersCache: MutableList<Any>
            get() = localObserver.dirtyLayers

        fun begin() {
            localObserver.isEnabled = true
            localObserver.dirtyLayers.addAll(originalDirtyLayers)
        }

        fun end() {
            localObserver.isEnabled = false
            localObserver.dirtyLayers.clear()
        }

        private class Observer : MutableListObserver.Observer<Any> {

            val dirtyLayers = ArrayList<Any>()

            var isEnabled = false

            override fun onAdded(element: Any) {
                if (isEnabled)
                    dirtyLayers += element
            }
        }
    }

    private data class NodeBackup(
        val coordinator: Any,
        val wrapped: Any,
        val wrappedBy: Any
    )

    private companion object {
        const val TAG = "AndroidComposeViewDescriptor"

        val EDGE_EFFECT_WORKAROUND_OVERSCROLL_EFFECT = "androidx.compose.foundation.AndroidEdgeEffectOverscrollEffect".toClass()
        val DRAW_OVERSCROLL_MODIFIER = "androidx.compose.foundation.DrawOverscrollModifier".toClass()
        val DRAW_STRETCH_OVERSCROLL_MODIFIER = "androidx.compose.foundation.DrawStretchOverscrollModifier".toClass()
        val MUTABLE_OBJECT_LIST_CLASS = "androidx.collection.MutableObjectList".toClass()
        val SCROLLING_CONTAINER_ELEMENT_CLASS = "androidx.compose.foundation.ScrollingContainerElement".toClass()
        val SCROLLABLE_AREA_ELEMENT_CLASS = "androidx.compose.foundation.ScrollableAreaElement".toClass()
        val SHARED_BOUNDS_NODE_CLASS = "androidx.compose.animation.SharedBoundsNode".toClass()
    }
}
