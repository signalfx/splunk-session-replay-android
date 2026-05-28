package com.splunk.android.instrumentation.recording.wireframe.extension

internal fun Class<out Any>.isInheritedBy(clazz: Class<out Any>): Boolean {
    return inheritedLevel(clazz) != -1
}

/**
 * @return -1 if this class is not inherited by [clazz].
 */
internal fun Class<out Any>.inheritedLevel(clazz: Class<out Any>): Int {
    var superclass: Class<*>? = clazz
    var level = 0

    do {
        if (superclass == this)
            return level

        level++
        superclass = superclass?.superclass
    } while (superclass != null)

    return -1
}

internal fun Class<out Any>.getAncestors(): List<String> {
    val result = ArrayList<String>()
    val rootClass = Object::class.java

    var superclass: Class<*>? = superclass

    while (superclass != null && superclass != rootClass) {
        result += superclass.name
        superclass = superclass.superclass
    }

    return result
}
