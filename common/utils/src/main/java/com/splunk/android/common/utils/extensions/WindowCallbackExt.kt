package com.splunk.android.common.utils.extensions

import android.app.Activity
import android.app.AlertDialog
import android.view.Window
import com.splunk.android.common.utils.window.WindowCallbackWrapper
import java.lang.reflect.Field

internal fun Window.Callback.getWindowCallbackChain(result: MutableList<Window.Callback> = ArrayList()): MutableList<Window.Callback> {
    var localCallback: Window.Callback? = this

    while (localCallback != null) {
        result += localCallback

        localCallback = if (localCallback is Activity || localCallback is AlertDialog)
            break
        else if (localCallback is WindowCallbackWrapper)
            localCallback.callback
        else {
            val callbackWrapperField = localCallback.findCallbackField() ?: break
            localCallback.get(callbackWrapperField)
        }
    }

    return result
}

internal fun Window.Callback.findCallbackField(): Field? {
    var windowCallbackClass: Class<*>? = this::class.java

    while (windowCallbackClass != null && windowCallbackClass != Object::class.java) {
        for (field in windowCallbackClass.declaredFields)
            if (field.type == Window.Callback::class.java)
                return field // Assume this is wrapped callback

        windowCallbackClass = windowCallbackClass.superclass
    }

    return null
}
