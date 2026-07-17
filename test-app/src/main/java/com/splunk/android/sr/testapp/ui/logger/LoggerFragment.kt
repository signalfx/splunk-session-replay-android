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

package com.splunk.android.sr.testapp.ui.logger

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.logger.extensions.toUserMessage
import com.splunk.android.sr.testapp.R
import com.splunk.android.sr.testapp.databinding.FragmentLoggerBinding
import com.splunk.android.sr.testapp.ui.BaseFragment

class LoggerFragment : BaseFragment<FragmentLoggerBinding>() {

    override val viewBindingCreator: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoggerBinding
        get() = FragmentLoggerBinding::inflate

    override val titleRes: Int = R.string.logger_title

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeLogs()

        viewBinding.logs.text = Logger.logs.toUserMessage()
    }

    private fun makeLogs() {
        for (i in 0 until 100)
            Logger.i(TAG, "Info message in loop with index $i")

        Logger.d(TAG, "Debug message")
        Logger.e(TAG, "Error message")
        Logger.i(TAG, "Info message")
        Logger.v(TAG, "Verbose message")
        Logger.w(TAG, "Warning message")

        try {
            arrayOf(0, 1, 2)[3]
        } catch (e: ArrayIndexOutOfBoundsException) {
            Logger.e(TAG, "Error while reading the array", e)
        }

        Logger.d(TAG, "Second debug message")
        Logger.d(TAG, "Second warning message")
    }

    private companion object {
        const val TAG = "LoggerFragment"
    }
}
