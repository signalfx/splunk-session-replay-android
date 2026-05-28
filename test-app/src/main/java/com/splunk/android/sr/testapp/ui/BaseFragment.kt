package com.splunk.android.sr.testapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.splunk.android.sr.testapp.util.FragmentAnimation
import kotlin.reflect.KClass

abstract class BaseFragment<T : ViewBinding> : Fragment() {

    private val activity: MainActivity
        get() = requireActivity() as MainActivity

    private var viewBindingInternal: T? = null

    protected abstract val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> T

    protected val viewBinding: T
        get() = viewBindingInternal ?: error("Must be called after createView()")

    abstract val titleRes: Int

    open val subtitleRes: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBindingInternal = viewBindingCreator(inflater, container, false)
        return viewBindingInternal?.root
    }

    override fun onDestroyView() {
        viewBindingInternal = null
        super.onDestroyView()
    }

    fun navigateTo(fragment: BaseFragment<*>, animation: FragmentAnimation? = FragmentAnimation.FADE) {
        activity.navigateTo(fragment, animation)
    }

    fun navigateTo(activityClass: KClass<out Activity>) {
        startActivity(Intent(requireContext(), activityClass.java))
    }
}
