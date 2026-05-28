package com.splunk.android.common.utils.extensions

import android.content.Context
import android.os.Build
import android.view.WindowManager
import com.splunk.android.common.utils.runOnAndroidAtLeast

val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

val Context.isUiContextCompat: Boolean
    get() = runOnAndroidAtLeast(Build.VERSION_CODES.S) { isUiContext } ?: true

private var fragmentSpecialEffectsControllerViewTag: Int? = 0

internal fun Context.getFragmentSpecialEffectsControllerViewTag(): Int? { // androidx.fragment.R.id.special_effects_controller_view_tag
    if (fragmentSpecialEffectsControllerViewTag == 0) {
        val name = "$packageName:id/special_effects_controller_view_tag"
        fragmentSpecialEffectsControllerViewTag = runCatching { resources.getValue(name).resourceId }.getOrNull()
    }

    return fragmentSpecialEffectsControllerViewTag
}
