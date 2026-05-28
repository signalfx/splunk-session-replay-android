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

package com.splunk.android.instrumentation.recording.wireframe.util

/**
 * List optimized for add elements in right order, for example Views sorted by z. Add element out of order has complexity O(N/2) in average and O(N) in worst case.
 */
internal class OrderedList<E>(initialCapacity: Int) {

    private var highestOrder = -Float.POSITIVE_INFINITY

    private val orders = ArrayList<Float>(initialCapacity)

    val values: MutableList<E> = ArrayList(initialCapacity)

    fun insert(order: Float, value: E) {
        if (order >= highestOrder) {
            highestOrder = order
            orders += order
            values += value
        } else
            for (i in orders.indices)
                if (orders[i] > order) {
                    orders.add(i, order)
                    values.add(i, value)
                    break
                }
    }

    fun isNotEmpty(): Boolean {
        return values.isNotEmpty()
    }
}
