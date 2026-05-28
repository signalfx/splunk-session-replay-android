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

package com.splunk.android.sr.testapp.ui.screenshot

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.splunk.android.instrumentation.recording.core.api.isSensitive
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentScreenshotViewsBinding
import com.splunk.android.sr.testapp.ui.adapter.SensitiveItemAdapter
import com.splunk.android.sr.testapp.ui.adapter.SpinnerAdapter
import com.splunk.android.sr.testapp.ui.dialog.AlertDialog
import com.splunk.android.sr.testapp.ui.dialog.BottomSheetDialogFragment
import com.splunk.android.sr.testapp.ui.dialog.DialogActivity
import com.splunk.android.sr.testapp.ui.dialog.DialogFragment
import com.splunk.android.sr.testapp.ui.dialog.WindowManagerDialog
import com.splunk.android.sr.testapp.ui.dialog.WindowManagerToast
import com.splunk.android.sr.testapp.ui.BaseFragment
import com.splunk.android.sr.testapp.ui.wireframe.model.SimpleItem
import com.splunk.android.sr.testapp.util.randomColor

class ScreenshotViewsFragment : BaseFragment<FragmentScreenshotViewsBinding>() {

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentScreenshotViewsBinding
        get() = FragmentScreenshotViewsBinding::inflate

    override val titleRes: Int = R.string.screenshot_fragment_views_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.textureView2Sensitivity.setOnClickListener(onClickListener)
        viewBinding.dialog1.setOnClickListener(onClickListener)
        viewBinding.dialog2.setOnClickListener(onClickListener)
        viewBinding.dialog3.setOnClickListener(onClickListener)
        viewBinding.dialog4.setOnClickListener(onClickListener)
        viewBinding.dialog5.setOnClickListener(onClickListener)
        viewBinding.dialog6.setOnClickListener(onClickListener)
        viewBinding.sensitivityAnimation.setOnClickListener(onClickListener)
        viewBinding.toast1.setOnClickListener(onClickListener)
        viewBinding.toast2.setOnClickListener(onClickListener)

        val items = (0..3).map { SimpleItem(it, "Title $it", "Description of $it", randomColor()) }
        val spinnerAdapter = SpinnerAdapter(requireContext(), items)

        viewBinding.spinner1.adapter = spinnerAdapter

        viewBinding.dialog1.isSensitive = true
        viewBinding.textViewSensitive1.isSensitive = true
        viewBinding.textViewSensitive2.isSensitive = true
        viewBinding.textViewSensitive3.isSensitive = true

        viewBinding.surfaceView2.setZOrderOnTop(true)

        viewBinding.sensitiveList.adapter = SensitiveItemAdapter(requireContext(), 50)

        initPlayer()
    }

    private fun initPlayer() {
        val mediaItem = MediaItem.Builder()
            .setUri(RawResourceDataSource.buildRawResourceUri(R.raw.rainy_day))
            .build()

        val player = ExoPlayer.Builder(requireContext())
            .build()

        player.setMediaItem(mediaItem)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.volume = 0f
        player.prepare()

        viewBinding.textureView2.player = player
    }

    override fun onResume() {
        super.onResume()
        viewBinding.surfaceView1.onResume()
        viewBinding.surfaceView2.onResume()
    }

    override fun onPause() {
        viewBinding.surfaceView1.onPause()
        viewBinding.surfaceView2.onResume()
        viewBinding.textureView2.player?.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        viewBinding.textureView2.player?.release()
        super.onDestroyView()
    }

    private fun toggleSensitiveViewVisibility() {
        val isVisible = viewBinding.textViewSensitive2.visibility == View.VISIBLE
        viewBinding.textViewSensitive2.visibility = if (isVisible) View.GONE else View.VISIBLE
        viewBinding.cover2.visibility = if (isVisible) View.GONE else View.VISIBLE
    }

    private val onClickListener = View.OnClickListener {
        when (it.id) {
            viewBinding.textureView2Sensitivity.id ->
                viewBinding.textureView2.videoSurfaceView?.isSensitive = viewBinding.textureView2.videoSurfaceView?.isSensitive != true
            viewBinding.dialog1.id ->
                AlertDialog.show(requireContext(), true)
            viewBinding.dialog2.id ->
                AlertDialog.show(requireContext(), false)
            viewBinding.dialog3.id ->
                DialogFragment().show(childFragmentManager, "DialogFragment")
            viewBinding.dialog4.id ->
                startActivity(Intent(requireContext(), DialogActivity::class.java))
            viewBinding.dialog5.id ->
                BottomSheetDialogFragment().show(childFragmentManager, "BottomSheetDialogFragment")
            viewBinding.dialog6.id ->
                WindowManagerDialog.show(requireContext())
            viewBinding.toast1.id ->
                Toast.makeText(requireContext(), "Sample Toast", Toast.LENGTH_LONG).show()
            viewBinding.toast2.id ->
                WindowManagerToast.show(requireContext())
            viewBinding.sensitivityAnimation.id ->
                toggleSensitiveViewVisibility()
        }
    }
}
