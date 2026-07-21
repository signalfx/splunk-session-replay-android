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
import com.splunk.rum.common.utils.Colors
import com.splunk.rum.common.utils.MutableListObserver
import com.splunk.rum.common.utils.extensions.calcCiscoId
import com.splunk.rum.common.utils.extensions.findFast
import com.splunk.rum.common.utils.extensions.hasDimBehind
import com.splunk.rum.common.utils.extensions.identity
import com.splunk.rum.common.utils.extensions.plusAssign
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.AbsSeekBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ActionMenuViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.AutoCompleteTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.BridgeInterfaceDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CalendarViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CheckBoxDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CheckedTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.CompoundButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.DatePickerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.EditTextDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.FrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.GridViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.HorizontalScrollViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ImageViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.LinearLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ListViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.NumberPickerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ProgressBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RadioButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RadioGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RatingBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.RelativeLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ScrollViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SeekBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SpaceDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SpinnerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SurfaceViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.SwitchDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TableLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TableRowDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TextureViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.TimePickerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ToggleButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.VideoViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewAnimatorDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.ViewGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.WebViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.AlertDialogLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.ButtonBarLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.DayPickerViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.DayPickerViewPagerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.DecorViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.DialogViewAnimatorDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.InternalViewPagerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.ListMenuItemViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.MenuPopupWindowMenuDropDownListViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.NumericTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.PhoneWindowDecorViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.PopupWindowPopupBackgroundViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.PopupWindowPopupDecorViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.PopupWindowPopupViewContainerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.RadialTimePickerViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.SimpleMonthViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.internal.TextInputTimePickerViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal.ActionMenuPresenterOverflowMenuButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal.AlertControllerRecycleListViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.internal.DropDownListViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.menu.ActionMenuItemViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.menu.ListMenuItemViewCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ActionBarContainerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ActionBarContextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ActionBarOverlayLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ActionMenuViewCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AlertDialogLayoutCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatAutoCompleteTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatCheckBoxDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatCheckedTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatEditTextDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatImageViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatRadioButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatRatingBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatSeekBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatSpinnerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.AppCompatToggleButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ButtonBarLayoutCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ContentFrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.DialogTitleDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.FitWindowsFrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.FitWindowsLinearLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.LinearLayoutCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.MenuPopupWindowMenuDropDownListViewCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.SearchViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.SearchViewSearchAutoCompleteDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.SwitchCompatDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.ToolbarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.cardview.CardViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart.BarChartDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart.BarLineChartBaseDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart.ChartDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.chart.HorizontalBarChartDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.AndroidComposeViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.AndroidViewHolderDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.AndroidViewsHandlerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.ComposeViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.RippleContainerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.compose.ViewFactoryHolderDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.constraintlayout.ConstraintLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.coordinatorlayout.CoordinatorLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core.ContentLoadingProgressBarDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core.NestedScrollViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.core.ScrollingViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.drawerlayout.DrawerLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.drawerlayout.FixedDrawerLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.flexbox.FlexboxLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.fragment.FragmentContainerViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.gms.MapViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.appbar.AppBarLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.appbar.CollapsingToolbarLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.bottomnavigation.BottomNavigationItemViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.bottomnavigation.BottomNavigationMenuViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.button.MaterialButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.card.MaterialCardViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.checkbox.MaterialCheckBoxDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.chip.ChipDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.chip.ChipGroupDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.floatingactionbutton.FloatingActionButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.imageview.ShapeableImageViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.BaselineLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.CheckableImageButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.FlowLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.NavigationMenuItemViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.NavigationMenuViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.internal.ScrimInsetsFrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.navigation.NavigationViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.radiobutton.MaterialRadioButtonDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.snackbar.SnackbarContentLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.snackbar.SnackbarSnackbarLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.switchmaterial.SwitchMaterialDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.tabs.TabLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.tabs.TabLayoutSlidingTabIndicatorDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.tabs.TabLayoutTabViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textfield.TextInputEditTextDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textfield.TextInputLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.textview.MaterialTextViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.percentlayout.PercentFrameLayoutDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.preference.PreferenceImageViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.recyclerview.RecyclerViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.swiperefreshlayout.CircleImageViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.swiperefreshlayout.SwipeRefreshLayoutDescription
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.vico.CartesianChartViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.vico.ChartViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager.ViewPagerDescriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager2.ViewPager2Descriptor
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.viewpager2.ViewPager2RecyclerViewImplDescriptor
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
    private val descriptorsInternal = ArrayList<ViewDescriptor>()

    private var isDescriptorListChanged = false

    /**
     * [ViewDescriptor] collection where each item describes a [View] implementation.
     */
    val descriptors: MutableCollection<ViewDescriptor> = MutableListObserver(descriptorsInternal, DescriptorsObserver())

    /**
     * [FragmentTypeIdentifier] collection where each item identify Fragment implementation.
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
        addBuiltinDescriptors()
        addBuiltinIdentifiers()

        BridgeManager.listeners += BridgeManagerListener()
    }

    internal fun extract(view: View): Window? {
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

    private fun addBuiltinDescriptors() {
        descriptors += RippleContainerDescriptor()
        descriptors += ViewFactoryHolderDescriptor()
        descriptors += AndroidViewHolderDescriptor()
        descriptors += AndroidViewsHandlerDescriptor()
        descriptors += PopupWindowPopupViewContainerDescriptor()
        descriptors += DialogViewAnimatorDescriptor()
        descriptors += ViewAnimatorDescriptor()
        descriptors += ContentLoadingProgressBarDescriptor()
        descriptors += DatePickerDescriptor()
        descriptors += ShapeableImageViewDescriptor()
        descriptors += PhoneWindowDecorViewDescriptor()
        descriptors += PercentFrameLayoutDescriptor()
        descriptors += CollapsingToolbarLayoutDescriptor()
        descriptors += SearchViewDescriptor()
        descriptors += MaterialCheckBoxDescriptor()
        descriptors += TableRowDescriptor()
        descriptors += HorizontalBarChartDescriptor()
        descriptors += BarChartDescriptor()
        descriptors += BarLineChartBaseDescriptor()
        descriptors += ChartDescriptor()
        descriptors += GridViewDescriptor()
        descriptors += MaterialCardViewDescriptor()
        descriptors += TextInputTimePickerViewDescriptor()
        descriptors += SimpleMonthViewDescriptor()
        descriptors += DayPickerViewPagerDescriptor()
        descriptors += DayPickerViewDescriptor()
        descriptors += ChipDescriptor()
        descriptors += ChipGroupDescriptor()
        descriptors += FlowLayoutDescriptor()
        descriptors += FixedDrawerLayoutDescriptor()
        descriptors += ActionBarContextViewDescriptor()
        descriptors += SearchViewSearchAutoCompleteDescriptor()
        descriptors += AppCompatAutoCompleteTextViewDescriptor()
        descriptors += AutoCompleteTextViewDescriptor()
        descriptors += ActionBarContainerDescriptor()
        descriptors += BottomNavigationItemViewDescriptor()
        descriptors += BaselineLayoutDescriptor()
        descriptors += FlexboxLayoutDescriptor()
        descriptors += CircleImageViewDescriptor()
        descriptors += BottomNavigationMenuViewDescriptor()
        descriptors += ActionBarOverlayLayoutDescriptor()
        descriptors += MenuPopupWindowMenuDropDownListViewDescriptor()
        descriptors += ListMenuItemViewDescriptor()
        descriptors += AppCompatCheckBoxDescriptor()
        descriptors += CheckBoxDescriptor()
        descriptors += AppBarLayoutDescriptor()
        descriptors += SnackbarContentLayoutDescriptor()
        descriptors += SnackbarSnackbarLayoutDescriptor()
        descriptors += FloatingActionButtonDescriptor()
        descriptors += PreferenceImageViewDescriptor()
        descriptors += DialogTitleDescriptor()
        descriptors += ButtonBarLayoutCompatDescriptor()
        descriptors += AlertControllerRecycleListViewDescriptor()
        descriptors += SpaceDescriptor()
        descriptors += ListMenuItemViewCompatDescriptor()
        descriptors += ActionMenuPresenterOverflowMenuButtonDescriptor()
        descriptors += MenuPopupWindowMenuDropDownListViewCompatDescriptor()
        descriptors += DropDownListViewDescriptor()
        descriptors += ListViewDescriptor()
        descriptors += AlertDialogLayoutCompatDescriptor()
        descriptors += PopupWindowPopupBackgroundViewDescriptor()
        descriptors += AppCompatCheckedTextViewDescriptor()
        descriptors += CheckedTextViewDescriptor()
        descriptors += NavigationMenuViewDescriptor()
        descriptors += NavigationMenuItemViewDescriptor()
        descriptors += NavigationViewDescriptor()
        descriptors += ScrimInsetsFrameLayoutDescriptor()
        descriptors += ActionMenuItemViewDescriptor()
        descriptors += SwipeRefreshLayoutDescription()
        descriptors += CardViewDescriptor()
        descriptors += PopupWindowPopupDecorViewDescriptor()
        descriptors += ActionMenuViewCompatDescriptor()
        descriptors += LinearLayoutCompatDescriptor()
        descriptors += ActionMenuViewDescriptor()
        descriptors += TabLayoutTabViewDescriptor()
        descriptors += TabLayoutSlidingTabIndicatorDescriptor()
        descriptors += TabLayoutDescriptor()
        descriptors += ViewPager2RecyclerViewImplDescriptor()
        descriptors += AppCompatRatingBarDescriptor()
        descriptors += RatingBarDescriptor()
        descriptors += SwitchMaterialDescriptor()
        descriptors += MaterialButtonDescriptor()
        descriptors += AppCompatButtonDescriptor()
        descriptors += MaterialRadioButtonDescriptor()
        descriptors += AppCompatRadioButtonDescriptor()
        descriptors += RadioButtonDescriptor()
        descriptors += MaterialTextViewDescriptor()
        descriptors += AppCompatSpinnerDescriptor()
        descriptors += AppCompatSeekBarDescriptor()
        descriptors += SeekBarDescriptor()
        descriptors += RadioGroupDescriptor()
        descriptors += NumericTextViewDescriptor()
        descriptors += AppCompatToggleButtonDescriptor()
        descriptors += ToggleButtonDescriptor()
        descriptors += CheckableImageButtonDescriptor()
        descriptors += AppCompatImageViewDescriptor()
        descriptors += AppCompatImageButtonDescriptor()
        descriptors += ImageButtonDescriptor()
        descriptors += TextInputEditTextDescriptor()
        descriptors += AppCompatEditTextDescriptor()
        descriptors += AppCompatTextViewDescriptor()
        descriptors += DrawerLayoutDescriptor()
        descriptors += CoordinatorLayoutDescriptor()
        descriptors += RadialTimePickerViewDescriptor()
        descriptors += ViewPager2Descriptor()
        descriptors += RecyclerViewDescriptor()
        descriptors += AndroidComposeViewDescriptor()
        descriptors += ComposeViewDescriptor()
        descriptors += SpinnerDescriptor()
        descriptors += ButtonBarLayoutDescriptor()
        descriptors += AlertDialogLayoutDescriptor()
        descriptors += ToolbarDescriptor()
        descriptors += FitWindowsFrameLayoutDescriptor()
        descriptors += FitWindowsLinearLayoutDescriptor()
        descriptors += FragmentContainerViewDescriptor()
        descriptors += ContentFrameLayoutDescriptor()
        descriptors += DecorViewDescriptor()
        descriptors += ConstraintLayoutDescriptor()
        descriptors += TableLayoutDescriptor()
        descriptors += RelativeLayoutDescriptor()
        descriptors += FrameLayoutDescriptor()
        descriptors += LinearLayoutDescriptor()
        descriptors += ScrollViewDescriptor()
        descriptors += NumberPickerDescriptor()
        descriptors += TimePickerDescriptor()
        descriptors += ViewPagerDescriptor()
        descriptors += InternalViewPagerDescriptor()
        descriptors += SurfaceViewDescriptor()
        descriptors += TextureViewDescriptor()
        descriptors += VideoViewDescriptor()
        descriptors += WebViewDescriptor()
        descriptors += CalendarViewDescriptor()
        descriptors += NestedScrollViewDescriptor()
        descriptors += MapViewDescriptor()
        descriptors += TextInputLayoutDescriptor()
        descriptors += EditTextDescriptor()
        descriptors += ScrollingViewDescriptor()
        descriptors += AbsSeekBarDescriptor()
        descriptors += ProgressBarDescriptor()
        descriptors += SwitchCompatDescriptor()
        descriptors += SwitchDescriptor()
        descriptors += CompoundButtonDescriptor()
        descriptors += ButtonDescriptor()
        descriptors += TextViewDescriptor()
        descriptors += ImageViewDescriptor()
        descriptors += HorizontalScrollViewDescriptor()
        descriptors += ChartViewDescriptor()
        descriptors += CartesianChartViewDescriptor()
    }

    private fun addBuiltinIdentifiers() {
        identifiers += StandardFragmentTypeIdentifier()
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
