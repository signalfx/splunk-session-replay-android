package com.splunk.android.sr.testapp.ui.wireframe

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentBridgeInterfaceBinding
import com.splunk.android.sr.testapp.ui.BaseFragment

class BridgeInterfaceFragment : BaseFragment<FragmentBridgeInterfaceBinding>() {

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBridgeInterfaceBinding
        get() = FragmentBridgeInterfaceBinding::inflate

    override val titleRes: Int = R.string.bridge_interface_title

    private val animator by lazy { ObjectAnimator.ofFloat(viewBinding.rectangle, "rotation", 0f, 360f) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.duration = 1000L
        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animator.cancel()
    }
}
