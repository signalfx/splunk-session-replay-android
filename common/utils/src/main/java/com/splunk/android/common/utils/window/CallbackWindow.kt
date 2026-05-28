package com.splunk.android.common.utils.window

import android.view.View
import android.view.Window
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.set

internal sealed interface CallbackWindow {

    var callback: Window.Callback?

    class Impl21(rootView: View) : CallbackWindow {

        private val window: Any = rootView.get("this\$0")!!

        override var callback: Window.Callback?
            get() {
                return try {
                    window.get("mCallback")
                } catch (e: NoSuchFieldException) {
                    Logger.e1(TAG, "getCallback", e)
                    null
                }
            }
            set(value) {
                try {
                    window.set("mCallback", value)
                } catch (e: NoSuchFieldException) {
                    Logger.e1(TAG, "setCallback", e)
                }
            }

        private companion object {
            const val TAG = "Impl21"
        }
    }

    class Impl24(rootView: View) : CallbackWindow {

        private val window: Window = rootView.get("mWindow")!!

        override var callback: Window.Callback?
            get() {
                return window.callback
            }
            set(value) {
                window.callback = value
            }
    }
}
