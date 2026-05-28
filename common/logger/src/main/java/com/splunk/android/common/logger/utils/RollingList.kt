package com.splunk.android.common.logger.utils

import java.util.LinkedList

internal class RollingList<E>(capacity: Int) : List<E> {

    private val list = LinkedList<E>()

    var capacity: Int = capacity
        set(value) {
            field = value

            if (value < size)
                for (i in 0 until size - value)
                    list.removeFirst()
        }

    override val size: Int
        get() = list.size

    fun add(element: E) {
        list.add(element)

        if (size > capacity)
            list.removeFirst()
    }

    operator fun plusAssign(element: E) {
        add(element)
    }

    fun clear() {
        list.clear()
    }

    override fun get(index: Int): E {
        return list[index]
    }

    override fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    override fun iterator(): Iterator<E> {
        return list.iterator()
    }

    override fun listIterator(): ListIterator<E> {
        return list.listIterator()
    }

    override fun listIterator(index: Int): ListIterator<E> {
        return list.listIterator(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        return list.subList(fromIndex, toIndex)
    }

    override fun lastIndexOf(element: E): Int {
        return list.lastIndexOf(element)
    }

    override fun indexOf(element: E): Int {
        return list.indexOf(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        return list.containsAll(elements)
    }

    override fun contains(element: E): Boolean {
        return list.contains(element)
    }
}
