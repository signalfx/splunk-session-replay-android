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

package com.splunk.android.sr.testapp.ui.menu

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.splunk.android.common.utils.extensions.ciscoId
import com.splunk.android.common.utils.extensions.toRect
import com.splunk.android.instrumentation.recording.core.api.RecordingMask
import com.splunk.android.instrumentation.recording.core.api.RenderingMode
import com.splunk.android.instrumentation.recording.core.api.SessionReplay
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentMenuBinding
import com.splunk.android.sr.testapp.extension.screenSize
import com.splunk.android.sr.testapp.ui.BaseFragment
import com.splunk.android.sr.testapp.ui.compose.CameraComposeActivity
import com.splunk.android.sr.testapp.ui.compose.DrawOrderComposeActivity
import com.splunk.android.sr.testapp.ui.compose.DrawRecompositionComposeActivity
import com.splunk.android.sr.testapp.ui.compose.LeakActivity
import com.splunk.android.sr.testapp.ui.compose.ListComposeActivity
import com.splunk.android.sr.testapp.ui.compose.MeasureRecompositionComposeActivity
import com.splunk.android.sr.testapp.ui.compose.SensitivityComposeActivity
import com.splunk.android.sr.testapp.ui.compose.SurfaceComposeActivity
import com.splunk.android.sr.testapp.ui.compose.TextFieldComposeActivity
import com.splunk.android.sr.testapp.ui.compose.VariantsActivity
import com.splunk.android.sr.testapp.ui.compose.VideoComposeActivity
import com.splunk.android.sr.testapp.ui.compose.ViewDrawOrderComposeActivity
import com.splunk.android.sr.testapp.ui.compose.WebViewComposeActivity
import com.splunk.android.sr.testapp.ui.interaction.FocusActivity
import com.splunk.android.sr.testapp.ui.logger.LoggerFragment
import com.splunk.android.sr.testapp.ui.screenshot.AnimationFragment
import com.splunk.android.sr.testapp.ui.screenshot.ScreenshotRegionsFragment
import com.splunk.android.sr.testapp.ui.screenshot.ScreenshotViewsFragment
import com.splunk.android.sr.testapp.ui.slowrendering.SlowRenderingFragment
import com.splunk.android.sr.testapp.ui.wireframe.BridgeInterfaceFragment
import com.splunk.android.sr.testapp.ui.wireframe.CollapsingLayoutFragment
import com.splunk.android.sr.testapp.ui.wireframe.EmptyActivity
import com.splunk.android.sr.testapp.ui.wireframe.ListFragment
import com.splunk.android.sr.testapp.ui.wireframe.WebViewFragment
import com.splunk.android.sr.testapp.ui.wireframe.WireframeViewsFragment
import com.splunk.android.sr.testapp.util.FragmentAnimation

class MenuFragment : BaseFragment<FragmentMenuBinding>() {

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMenuBinding
        get() = FragmentMenuBinding::inflate

    override val titleRes: Int = R.string.menu_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.changeRecordingMode.setOnClickListener(onClickListener)
        viewBinding.screenshotViews.setOnClickListener(onClickListener)
        viewBinding.screenshotRegions.setOnClickListener(onClickListener)
        viewBinding.screenshotMasks.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationNone.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationFade1.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationFade2.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationFade.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationTranslate1.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationTranslate2.setOnClickListener(onClickListener)
        viewBinding.screenshotFragmentAnimationTranslate.setOnClickListener(onClickListener)
        viewBinding.interactionsFocus.setOnClickListener(onClickListener)
        viewBinding.wireframeViews.setOnClickListener(onClickListener)
        viewBinding.wireframeCollapsingLayout.setOnClickListener(onClickListener)
        viewBinding.wireframeOrdering.setOnClickListener(onClickListener)
        viewBinding.wireframeList.setOnClickListener(onClickListener)
        viewBinding.wireframeWebView.setOnClickListener(onClickListener)
        viewBinding.bridgeInterface.setOnClickListener(onClickListener)
        viewBinding.slowRendering.setOnClickListener(onClickListener)
        viewBinding.composeList.setOnClickListener(onClickListener)
        viewBinding.composeSensitivity.setOnClickListener(onClickListener)
        viewBinding.composeDrawOrder.setOnClickListener(onClickListener)
        viewBinding.composeViewDrawOrder.setOnClickListener(onClickListener)
        viewBinding.composeDrawRecomposition.setOnClickListener(onClickListener)
        viewBinding.composeMeasureRecomposition.setOnClickListener(onClickListener)
        viewBinding.composeSurface.setOnClickListener(onClickListener)
        viewBinding.composeCamera.setOnClickListener(onClickListener)
        viewBinding.composeVideo.setOnClickListener(onClickListener)
        viewBinding.composeTextField.setOnClickListener(onClickListener)
        viewBinding.composeWebView.setOnClickListener(onClickListener)
        viewBinding.composeActivityLeak.setOnClickListener(onClickListener)
        viewBinding.composeVariants.setOnClickListener(onClickListener)
        viewBinding.logger.setOnClickListener(onClickListener)

        viewBinding.wireframeViews.isSensitive = true

        viewBinding.screenshotViews.ciscoId = "screenshot_views_button"
    }

    private fun toggleMasks() {
        SessionReplay.instance.recordingMask = if (SessionReplay.instance.recordingMask == null) {
            val screenSize = requireContext().screenSize
            val w = screenSize.width
            val h = screenSize.height

            RecordingMask(
                listOf(
                    RecordingMask.Element(Rect(0, 0, w, h), RecordingMask.Element.Type.COVERING),
                    RecordingMask.Element(RectF(w * 0.1f, w * 0.1f, w * 0.9f, h - w * 0.1f).toRect(), RecordingMask.Element.Type.ERASING),
                    RecordingMask.Element(RectF(w * 0.2f, w * 0.2f, w * 0.45f, w * 0.5f).toRect(), RecordingMask.Element.Type.COVERING),
                    RecordingMask.Element(RectF(w * 0.55f, w * 0.2f, w * 0.8f, w * 0.5f).toRect(), RecordingMask.Element.Type.COVERING),
                    RecordingMask.Element(RectF(w * 0.05f, w * 0.3f, w * 0.95f, w * 0.4f).toRect(), RecordingMask.Element.Type.ERASING),
                    RecordingMask.Element(RectF(w * 0.475f, w * 0.15f, w * 0.525f, w * 0.55f).toRect(), RecordingMask.Element.Type.COVERING),
                    RecordingMask.Element(RectF(w * 0.4f, w * 0.25f, w * 0.6f, w * 0.45f).toRect(), RecordingMask.Element.Type.ERASING)
                )
            )
        } else
            null
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            viewBinding.changeRecordingMode.id -> {
                when (SessionReplay.instance.state.renderingMode) {
                    RenderingMode.NATIVE -> SessionReplay.instance.preferences.renderingMode = RenderingMode.WIREFRAME_ONLY
                    RenderingMode.WIREFRAME_ONLY -> SessionReplay.instance.preferences.renderingMode = RenderingMode.NATIVE
                }
            }
            viewBinding.screenshotViews.id ->
                navigateTo(ScreenshotViewsFragment())
            viewBinding.screenshotRegions.id ->
                navigateTo(ScreenshotRegionsFragment())
            viewBinding.screenshotMasks.id ->
                toggleMasks()
            viewBinding.screenshotFragmentAnimationNone.id ->
                navigateTo(AnimationFragment())
            viewBinding.screenshotFragmentAnimationFade1.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_FADE_1)
            viewBinding.screenshotFragmentAnimationFade2.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_FADE_2)
            viewBinding.screenshotFragmentAnimationFade.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_FADE)
            viewBinding.screenshotFragmentAnimationTranslate1.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_TRANSLATE_1)
            viewBinding.screenshotFragmentAnimationTranslate2.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_TRANSLATE_2)
            viewBinding.screenshotFragmentAnimationTranslate.id ->
                navigateTo(AnimationFragment(), FragmentAnimation.SLOW_TRANSLATE)
            viewBinding.wireframeViews.id ->
                navigateTo(WireframeViewsFragment())
            viewBinding.wireframeCollapsingLayout.id ->
                navigateTo(CollapsingLayoutFragment())
            viewBinding.wireframeOrdering.id ->
                navigateTo(EmptyActivity::class)
            viewBinding.wireframeList.id ->
                navigateTo(ListFragment())
            viewBinding.interactionsFocus.id ->
                navigateTo(FocusActivity::class)
            viewBinding.composeList.id ->
                navigateTo(ListComposeActivity::class)
            viewBinding.composeSensitivity.id ->
                navigateTo(SensitivityComposeActivity::class)
            viewBinding.composeDrawOrder.id ->
                navigateTo(DrawOrderComposeActivity::class)
            viewBinding.composeViewDrawOrder.id ->
                navigateTo(ViewDrawOrderComposeActivity::class)
            viewBinding.composeDrawRecomposition.id ->
                navigateTo(DrawRecompositionComposeActivity::class)
            viewBinding.composeMeasureRecomposition.id ->
                navigateTo(MeasureRecompositionComposeActivity::class)
            viewBinding.composeSurface.id ->
                navigateTo(SurfaceComposeActivity::class)
            viewBinding.composeCamera.id ->
                navigateTo(CameraComposeActivity::class)
            viewBinding.composeVideo.id ->
                navigateTo(VideoComposeActivity::class)
            viewBinding.composeTextField.id ->
                navigateTo(TextFieldComposeActivity::class)
            viewBinding.composeWebView.id ->
                navigateTo(WebViewComposeActivity::class)
            viewBinding.wireframeWebView.id ->
                navigateTo(WebViewFragment())
            viewBinding.composeActivityLeak.id ->
                navigateTo(LeakActivity::class)
            viewBinding.composeVariants.id ->
                navigateTo(VariantsActivity::class)
            viewBinding.bridgeInterface.id ->
                navigateTo(BridgeInterfaceFragment())
            viewBinding.slowRendering.id ->
                navigateTo(SlowRenderingFragment())
            viewBinding.logger.id ->
                navigateTo(LoggerFragment())
        }
    }
}
