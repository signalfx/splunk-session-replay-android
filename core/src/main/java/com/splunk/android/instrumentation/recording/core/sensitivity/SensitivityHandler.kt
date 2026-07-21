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

package com.splunk.android.instrumentation.recording.core.sensitivity

import android.os.Build
import android.view.View
import android.webkit.WebView
import android.widget.EditText
import com.splunk.android.instrumentation.recording.capturer.FrameCapturer
import com.splunk.android.instrumentation.recording.core.R

internal var View.sensitivityTag: Boolean?
    get() = when (val isSensitive = getTag(R.id.sr_sensitivity)) {
        is String -> isSensitive.toBooleanStrictOrNull()
        is Boolean -> isSensitive
        else -> null
    }
    set(isSensitive) {
        setTag(R.id.sr_sensitivity, isSensitive)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            FrameCapturer.requestNewFrame(this)
    }

internal class SensitivityHandler {

    companion object {
        private const val SENSITIVE = true
    }

    val sensitiveClasses = HierarchicallySortedClassMap<Boolean?>()

    init {
        sensitiveClasses[EditText::class.java] = SENSITIVE
        sensitiveClasses[WebView::class.java] = SENSITIVE
    }
}

internal class HierarchicallySortedClassMap<H> {

    private val internalList = mutableListOf<Pair<Class<*>, H>>()

    operator fun set(clazz: Class<*>, value: H) {
        internalList.forEachIndexed { index, pair ->
            if (pair.first == clazz) {
                internalList[index] = Pair(clazz, value)
                return
            } else if (pair.first.isAssignableFrom(clazz)) {
                internalList.add(index, Pair(clazz, value))
                return
            }
        }

        internalList.add(Pair(clazz, value))
    }

    operator fun get(clazz: Class<*>): H? =
        internalList.firstOrNull { it.first.isAssignableFrom(clazz) }?.second

    fun remove(clazz: Class<*>) = internalList.removeAll { it.first == clazz }
}
