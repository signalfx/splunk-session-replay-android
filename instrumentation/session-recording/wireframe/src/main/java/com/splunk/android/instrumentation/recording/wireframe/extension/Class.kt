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
