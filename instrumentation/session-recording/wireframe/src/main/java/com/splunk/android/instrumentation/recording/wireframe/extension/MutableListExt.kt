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

package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun <E> MutableList<E>.set(items: List<E>) {
    loop@ for (i in indices.reversed()) {
        val item = get(i)

        for (newItem in items)
            if (item == newItem)
                continue@loop

        removeAt(i)
    }

    loop@ for (i in items.indices) {
        val newItem = items[i]

        for (item in this)
            if (item == newItem)
                continue@loop

        add(newItem)
    }
}
