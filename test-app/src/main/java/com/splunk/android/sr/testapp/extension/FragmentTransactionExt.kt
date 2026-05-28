package com.splunk.android.sr.testapp.extension

import androidx.fragment.app.FragmentTransaction
import com.splunk.android.sr.testapp.util.FragmentAnimation

fun FragmentTransaction.setCustomAnimations(animation: FragmentAnimation?): FragmentTransaction {
    if (animation != null)
        return setCustomAnimations(animation.enter, animation.exit, animation.popEnter, animation.popExit)

    return this
}
