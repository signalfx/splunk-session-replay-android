package com.splunk.android.common.utils.set

class ListWrapper<E>(
    private val list: MutableList<E>
) : MutableSetWrapper.Wrapper<E> {

    override val size: Int
        get() = list.size

    override fun add(element: E): Boolean {
        return if (!contains(element))
            list.add(element)
        else
            false
    }

    override fun remove(element: E): Boolean {
        val iterator = iterator()

        for (e in iterator)
            if (e == element) {
                iterator.remove()
                return true
            }

        return false
    }

    override fun contains(element: E): Boolean {
        for (e in iterator())
            if (e == element)
                return true

        return false
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {

            var index = 0

            override fun hasNext(): Boolean {
                return index < list.lastIndex
            }

            override fun next(): E {
                return list[index++] ?: throw IllegalStateException(":(")
            }

            override fun remove() {
                list.removeAt(index)
            }
        }
    }

    override fun clear() {
        list.clear()
    }
}
