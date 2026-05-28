package com.splunk.android.sr.testapp.util

import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import com.splunk.android.sr.testapp.R

data class FragmentAnimation(
    @AnimatorRes @AnimRes val enter: Int,
    @AnimatorRes @AnimRes val exit: Int,
    @AnimatorRes @AnimRes val popEnter: Int = 0,
    @AnimatorRes @AnimRes val popExit: Int = 0
) {

    companion object {
        val FADE = FragmentAnimation(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
        val SLOW_FADE = FragmentAnimation(R.animator.slow_fade_in, R.animator.slow_fade_out, R.animator.slow_fade_in, R.animator.slow_fade_out)
        val SLOW_FADE_1 = FragmentAnimation(0, R.animator.slow_fade_out, 0, R.animator.slow_fade_out)
        val SLOW_FADE_2 = FragmentAnimation(R.animator.slow_fade_in, 0, R.animator.slow_fade_in, 0)
        val SLOW_TRANSLATE = FragmentAnimation(R.anim.slow_enter_from_right, R.anim.slow_exit_to_left, R.anim.slow_enter_from_left, R.anim.slow_exit_to_right)
        val SLOW_TRANSLATE_1 = FragmentAnimation(0, R.anim.slow_exit_to_left, 0, R.anim.slow_exit_to_right)
        val SLOW_TRANSLATE_2 = FragmentAnimation(R.anim.slow_enter_from_right, 0, R.anim.slow_enter_from_left, 0)
    }
}
