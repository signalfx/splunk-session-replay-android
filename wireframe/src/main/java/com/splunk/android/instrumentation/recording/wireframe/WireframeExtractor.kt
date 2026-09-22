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

package com.splunk.android.instrumentation.recording.wireframe

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.splunk.android.bridge.BridgeManager
import com.splunk.android.bridge.model.BridgeInterface
import com.splunk.android.instrumentation.recording.wireframe.descriptor.BridgeInterfaceDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.extension.getAncestors
import com.splunk.android.instrumentation.recording.wireframe.extension.inheritedLevel
import com.splunk.android.instrumentation.recording.wireframe.extension.isInvisibleForWireframe
import com.splunk.android.instrumentation.recording.wireframe.extension.withAlpha
import com.splunk.android.instrumentation.recording.wireframe.identifier.FragmentTypeIdentifier
import com.splunk.android.instrumentation.recording.wireframe.identifier.StandardFragmentTypeIdentifier
import com.splunk.android.instrumentation.recording.wireframe.model.ClassDefinition
import com.splunk.android.instrumentation.recording.wireframe.model.SensitivityDeterminer
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window
import com.splunk.android.instrumentation.recording.wireframe.stats.StatsCollector
import com.splunk.android.instrumentation.recording.wireframe.stats.WireframeStats
import com.splunk.android.instrumentation.recording.wireframe.util.isRunningVisibilityAnimation
import com.splunk.rum.common.utils.Colors
import com.splunk.rum.common.utils.MutableListObserver
import com.splunk.rum.common.utils.extensions.calcCiscoId
import com.splunk.rum.common.utils.extensions.findFast
import com.splunk.rum.common.utils.extensions.hasDimBehind
import com.splunk.rum.common.utils.extensions.identity
import com.splunk.rum.common.utils.extensions.plusAssign
import com.splunk.rum.common.utils.extensions.toClass

/* MARK
 *  Future optimizations
 *   - Deferred skeletons
 *   - Skeletons for only visible parts
 *   - Multi threaded skeletons
 */

// FIXME missing wireframe in split screen mode when app is on bottom part of screen.

/**
 * Extracts [Wireframe] data from screen.
 */
object WireframeExtractor {

    private val popupWindowDecorViewClass = "android.widget.PopupWindow\$PopupDecorView".toClass()
    private val popupWindowBackgroundViewClass = "android.widget.PopupWindow\$PopupBackgroundView".toClass()
    private val decorViewClass = "com.android.internal.policy.DecorView".toClass()
    private val decorViewClass21 = "com.android.internal.policy.impl.PhoneWindow\$DecorView".toClass()
    private val decorViewClass23 = "com.android.internal.policy.PhoneWindow\$DecorView".toClass()

    private val packageNameRegex = "\\.(?>debug|release|alpha|beta|dev|prod)\$".toRegex() // https://regex101.com/r/zy4fYh/2

    private val locationBuffer = IntArray(2)

    private val unknownViewClassesInternal = HashMap<String, ClassDefinition>()

    private val viewDescriptor = ViewDescriptor()
    private val viewGroupDescriptor = ViewGroupDescriptor()
    private val descriptorsInternal = ArrayList<ViewDescriptor>(200)

    private var areBuiltinsRegistered = false
    private var isDescriptorListChanged = false

    /**
     * [ViewDescriptor] collection where each item describes a [View] implementation.
     *
     * Builtin descriptors are not registered until the first extraction, so the collection contains only custom descriptors until then.
     */
    val descriptors: MutableCollection<ViewDescriptor> = MutableListObserver(descriptorsInternal, DescriptorsObserver())

    /**
     * [FragmentTypeIdentifier] collection where each item identify Fragment implementation.
     *
     * Builtin identifiers are not registered until the first extraction, so the collection contains only custom identifiers until then.
     */
    val identifiers: MutableCollection<FragmentTypeIdentifier> = ArrayList()

    /**
     * Returns [View]s that are unknown for description process. Description quality of unknown [View]s can be lower, because their description has not been checked by human.
     */
    val unknownViewClasses: List<ClassDefinition>
        get() = unknownViewClassesInternal
            .map { it.value }
            .sortedWith(compareBy({ it.isInternal }, { it.className }))

    /**
     * Whether all [Canvas] calls should be logged for debugging purpose.
     */
    var isCanvasCallsLoggingEnabled: Boolean
        get() = ViewDescriptor.isCanvasCallsLoggingEnabled
        set(value) {
            ViewDescriptor.isCanvasCallsLoggingEnabled = value
        }

    /**
     * Whether is simulated that all [View]s are unknown. It means, all [View]s will be described by [ViewDescriptor.ExtractionMode.CANVAS] mode.
     */
    var isUnknownViewsSimulationEnabled: Boolean = false

    var sensitivityDeterminer: SensitivityDeterminer?
        get() = ViewDescriptor.sensitivityDeterminer
        set(value) {
            ViewDescriptor.sensitivityDeterminer = value
        }

    init {
        BridgeManager.listeners += BridgeManagerListener()
    }

    internal fun extract(view: View): Window? {
        registerBuiltinsIfNeeded()

        StatsCollector.measureWindow {
            if (isDescriptorListChanged) {
                isDescriptorListChanged = false
                prepareDescriptors()
            }

            if (view.visibility != View.VISIBLE && !view.isRunningVisibilityAnimation || view.alpha == 0f || isInvisibleForWireframe(view))
                return null

            val layoutParams = view.layoutParams as? WindowManager.LayoutParams
            val windowSkeletons = ArrayList<Window.View.Skeleton>()
            val windowRect: Rect

            view.getLocationOnScreen(locationBuffer)

            val viewRect = Rect(0, 0, view.width, view.height)
            viewRect.offset(locationBuffer[0], locationBuffer[1])

            if (layoutParams?.hasDimBehind() == true) {
                windowRect = Rect(locationBuffer[0], locationBuffer[1], Int.MAX_VALUE, Int.MAX_VALUE)

                windowSkeletons += Window.View.Skeleton.Color(
                    type = Window.View.Skeleton.Color.Type.GENERAL,
                    colors = Colors(Color.BLACK.withAlpha(layoutParams.dimAmount)),
                    radii = null,
                    rect = Rect(Int.MIN_VALUE, Int.MIN_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
                    clipRect = null,
                    flags = null,
                    isOpaque = layoutParams.dimAmount == 1f
                )
            } else
                windowRect = Rect(viewRect)

            val viewDescription = extract(view, viewRect, viewRect, 1f, 1f, null)

            return Window(
                id = view.calcCiscoId(),
                rect = windowRect,
                skeletons = windowSkeletons.ifEmpty { null },
                subviews = listOf(viewDescription),
                identity = view.identity
            )
        }
    }

    internal fun popWireframeStats(): WireframeStats {
        return StatsCollector.popWireframeStats()
    }

    private fun extract(view: View, viewRect: Rect, clipRect: Rect, parentScaleX: Float, parentScaleY: Float, isParentSensitive: Boolean?): Window.View {
        var descriptor: ViewDescriptor? = null

        if (!isUnknownViewsSimulationEnabled)
            descriptor = descriptors.findFast { it.intendedClass?.isInstance(view) ?: false }

        if (descriptor == null)
            descriptor = when (view) {
                is ViewGroup -> viewGroupDescriptor
                else -> viewDescriptor
            }

        reportUnknownClassIfNeeded(view, descriptor)

        return descriptor.describe(view, viewRect, clipRect, parentScaleX, parentScaleY, isParentSensitive, ::extract, ::identifyFragmentType)
    }

    private fun reportUnknownClassIfNeeded(view: View, descriptor: ViewDescriptor) {
        if (view::class.java == descriptor.intendedClass || isUnknownViewsSimulationEnabled)
            return

        if (descriptor::class.java != ViewDescriptor::class.java && view::class.java != View::class.java || descriptor::class.java == ViewGroupDescriptor::class.java)
            reportUnknownClass(view)
    }

    private fun reportUnknownClass(view: View) {
        val name = view::class.java.name

        if (name !in unknownViewClassesInternal) {
            val packageName = view.context.packageName.replace(packageNameRegex, "")
            val ancestors = view::class.java.getAncestors()
            val isInternal = name.startsWith(packageName)

            unknownViewClassesInternal[name] = ClassDefinition(name, ancestors, isInternal)
        }
    }

    private fun registerBuiltinsIfNeeded() {
        if (areBuiltinsRegistered)
            return

        areBuiltinsRegistered = true

        descriptors += BuiltinDescriptors.create()
        identifiers += StandardFragmentTypeIdentifier()
    }

    private fun prepareDescriptors() {
        descriptorsInternal.removeAll { it.intendedClass == null }
        descriptorsInternal.sortByDescending { View::class.java.inheritedLevel(it.intendedClass!!) }
    }

    private fun identifyFragmentType(fragmentClass: Class<out Any>): Window.View.Type? {
        for (identifier in identifiers)
            return identifier.identify(fragmentClass) ?: continue

        return null
    }

    private fun isInvisibleForWireframe(view: View): Boolean {
        if (view.isInvisibleForWireframe)
            return true

        if (view is ViewGroup)
            when (view::class.java) {
                popupWindowDecorViewClass -> {
                    val child = view.getChildAt(0)

                    return if (child != null && child is ViewGroup && child::class.java == popupWindowBackgroundViewClass)
                        child.getChildAt(0)?.isInvisibleForWireframe == true
                    else
                        child?.isInvisibleForWireframe == true
                }
                decorViewClass, decorViewClass21, decorViewClass23 -> {
                    return view.findViewById<View>(android.R.id.content)?.isInvisibleForWireframe == true
                }
            }

        return view.isInvisibleForWireframe
    }

    private class DescriptorsObserver : MutableListObserver.Observer<ViewDescriptor> {
        override fun onAdded(element: ViewDescriptor) {
            isDescriptorListChanged = true
        }
    }

    private class BridgeManagerListener : BridgeManager.Listener {
        override fun onBridgeInterfaceAdded(bridge: BridgeInterface) {
            for (rootClass in bridge.obtainWireframeRootClasses())
                descriptors += BridgeInterfaceDescriptor(rootClass, bridge)
        }

        override fun onBridgeInterfaceRemoved(bridge: BridgeInterface) {
            val classes = bridge.obtainWireframeRootClasses()
            descriptors.removeAll { it.intendedClass != null && it.intendedClass in classes }
        }
    }
}
