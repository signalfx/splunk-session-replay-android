package com.splunk.android.common.utils.set

class MutableSetWrapper<E>(
    private val wrapper: Wrapper<E>
) : MutableSet<E> {

    override val size: Int
        get() = wrapper.size

    override fun contains(element: E): Boolean {
        return wrapper.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        for (element in elements)
            if (!contains(element))
                return false

        return true
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun add(element: E): Boolean {
        return wrapper.add(element)
    }

    override fun addAll(elements: Collection<E>): Boolean {
        var added = false

        for (element in elements)
            added = add(element) || added

        return added
    }

    override fun clear() {
        wrapper.clear()
    }

    override fun iterator(): MutableIterator<E> {
        return wrapper.iterator()
    }

    override fun remove(element: E): Boolean {
        return wrapper.remove(element)
    }

    override fun removeAll(elements: Collection<E>): Boolean {
        var removed = false

        for (element in elements)
            removed = remove(element) || removed

        return removed
    }

    override fun retainAll(elements: Collection<E>): Boolean {
        var removed = false

        for (element in elements)
            if (!contains(element))
                removed = remove(element) || removed

        return removed
    }

    interface Wrapper<E> {

        val size: Int

        fun add(element: E): Boolean

        fun remove(element: E): Boolean

        fun contains(element: E): Boolean

        fun iterator(): MutableIterator<E>

        fun clear()
    }
}
