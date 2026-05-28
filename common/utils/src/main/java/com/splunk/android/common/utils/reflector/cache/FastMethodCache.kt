package com.splunk.android.common.utils.reflector.cache

import java.lang.reflect.Method
import kotlin.math.max

internal class FastMethodCache(initialCapacity: Int = 8) {

    private var classes = arrayOfNulls<Class<*>>(initialCapacity)
    private var names = arrayOfNulls<String>(initialCapacity)
    private var returnTypes = arrayOfNulls<Class<*>>(initialCapacity)
    private var paramTypesArray = arrayOfNulls<Array<out Class<*>>>(initialCapacity)
    private var methods = arrayOfNulls<Method>(initialCapacity)

    private var size = 0

    @Suppress("EmptyRange")
    fun get(clazz: Class<*>, name: String, returnType: Class<*>, paramTypes: Array<out Class<*>>): Method? {
        loop@ for (i in 0 until size) {
            if (classes[i] === clazz && names[i] == name && returnTypes[i] == returnType) {
                val cachedTypes = paramTypesArray[i]!!

                if (cachedTypes.size == paramTypes.size) {
                    for (j in cachedTypes.indices)
                        if (cachedTypes[j] !== paramTypes[j])
                            break@loop

                    return methods[i]
                }
            }
        }

        return null
    }

    fun add(clazz: Class<*>, name: String, returnType: Class<*>, paramTypes: Array<out Class<*>>, method: Method) {
        if (size >= classes.size) {
            val newCap = max(size * 2, 1)
            classes = classes.copyOf(newCap)
            names = names.copyOf(newCap)
            returnTypes = returnTypes.copyOf(newCap)
            paramTypesArray = paramTypesArray.copyOf(newCap)
            methods = methods.copyOf(newCap)
        }

        classes[size] = clazz
        names[size] = name
        returnTypes[size] = returnType
        paramTypesArray[size] = paramTypes
        methods[size] = method

        size++
    }
}
