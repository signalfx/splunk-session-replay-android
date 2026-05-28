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
