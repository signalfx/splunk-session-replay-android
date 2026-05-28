package com.splunk.android.common.utils.extensions

import org.junit.Test

class ClassExtKtTest {

    @Test
    fun findMethod() {
        val testClass = TestClass()

        testClass.invoke<Void>("callVoid")
        testClass.invoke<Unit>("callVoid")
        testClass.invoke<Boolean>("getBoolean")
        testClass.invoke<Int>("getInt")
        testClass.invoke<Long>("getLong")
        testClass.invoke<Float>("getFloat")
        testClass.invoke<Double>("getDouble")
        testClass.invoke<String>("getString")
        testClass.invoke<TestClass.InnerClass>("getInnerClass")
        testClass.invoke<Any>("getInnerClass")

        assertThrows<NoSuchMethodException> { testClass.invoke<Boolean>("callVoid") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Void>("getBoolean") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Long>("getInt") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Float>("getLong") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Double>("getFloat") }
        assertThrows<NoSuchMethodException> { testClass.invoke<String>("getDouble") }
        assertThrows<NoSuchMethodException> { testClass.invoke<TestClass.InnerClass>("getString") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Boolean>("getInnerClass") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Int>("getInnerClass") }
    }

    @Test
    fun findMethodKt() {
        val testClass = TestClassKt()

        testClass.invoke<Void>("callVoid")
        testClass.invoke<Unit>("callVoid")
        testClass.invoke<Int>("getInt")
        testClass.invoke<TestClassKt.InnerClass>("getInnerClass")
        testClass.invoke<Any>("getInnerClass")

        assertThrows<NoSuchMethodException> { testClass.invoke<Int>("callVoid") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Long>("getInt") }
        assertThrows<NoSuchMethodException> { testClass.invoke<Unit>("getInnerClass") }
    }

    private inline fun <reified E : Exception> assertThrows(block: () -> Unit) {
        try {
            block()

            throw IllegalStateException("Method not throws exception")
        } catch (e: Exception) {
            if (e !is E)
                throw IllegalArgumentException("Method throws unexpected exception '$e'")
        }
    }
}
