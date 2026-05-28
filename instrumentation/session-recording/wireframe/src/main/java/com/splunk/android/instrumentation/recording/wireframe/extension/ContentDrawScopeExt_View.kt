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

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.reflector.Reflector
import com.splunk.android.instrumentation.recording.wireframe.util.ComposeInfo
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_2
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_3
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_4
import java.lang.reflect.Method

// MARK Requires Proguard rules

private val viewProvider: ViewProvider? = determineSuitableViewProvider()

internal fun ContentDrawScope.getAndroidView(): View? {
    return viewProvider?.findView(this)
}

private fun determineSuitableViewProvider(): ViewProvider? {
    return when {
        ComposeInfo.version >= VERSION_1_4 -> Compose14ViewProvider()
        ComposeInfo.version >= VERSION_1_3 -> Compose13ViewProvider()
        ComposeInfo.version >= VERSION_1_2 -> Compose12ViewProvider()
        else -> null
    }
}

private interface ViewProvider {
    fun findView(scope: ContentDrawScope): View?
}

private class Compose12ViewProvider : ViewProvider {

    private val reflector = Reflector(5, 0, 0)

    override fun findView(scope: ContentDrawScope): View? {
        return reflector.reflect {
            try {
                val drawEntity = scope.get<Any>("drawEntity") ?: return@reflect null
                val nextDrawEntity = drawEntity.get<Any>("next") ?: return@reflect null
                val modifier = nextDrawEntity.get<Any>("modifier") ?: return@reflect null
                val onDraw = modifier.get<Any>("onDraw") ?: return@reflect null
                val viewFactoryHolder = onDraw.get<ViewGroup>("this\$0") ?: return@reflect null
                viewFactoryHolder.getChildAt(0)
            } catch (e: NoSuchFieldException) {
                Logger.e1("Compose12ViewProvider", "findView", e)
                null
            }
        }
    }
}

private class Compose13ViewProvider : ViewProvider {

    private val nextDrawNodeMethod: Method? = findNextDrawNodeMethod()

    private val reflector = Reflector(4, 0, 0)

    override fun findView(scope: ContentDrawScope): View? {
        return reflector.reflect {
            try {
                val drawNode = scope.get<Any>("drawNode") ?: return@reflect null
                val nextDrawNode = nextDrawNodeMethod?.invoke(null, drawNode) ?: return@reflect null
                val element = nextDrawNode.get<Any>("element") ?: return@reflect null
                val onDraw = element.get<Any>("onDraw") ?: return@reflect null
                val viewFactoryHolder = onDraw.get<ViewGroup>("this\$0") ?: return@reflect null
                viewFactoryHolder.getChildAt(0)
            } catch (e: NoSuchFieldException) {
                Logger.e1("Compose13ViewProvider", "findView", e)
                null
            }
        }
    }

    private fun findNextDrawNodeMethod(): Method? { // TODO ReflectiveOperationException
        return try {
            val delegatableNodeClass = Class.forName("androidx.compose.ui.node.DelegatableNode")
            val layoutNodeDrawScopeKtClass = Class.forName("androidx.compose.ui.node.LayoutNodeDrawScopeKt")
            val method = layoutNodeDrawScopeKtClass.getDeclaredMethod("nextDrawNode", delegatableNodeClass)
            method.isAccessible = true
            method
        } catch (e: ClassNotFoundException) {
            Logger.e1("Compose13ViewProvider", "findNextDrawNodeMethod()", e)
            null
        } catch (e: NoSuchMethodException) {
            Logger.e1("Compose13ViewProvider", "findNextDrawNodeMethod()", e)
            null
        }
    }
}

private class Compose14ViewProvider : ViewProvider {

    private val reflector = Reflector(4, 0, 0)

    override fun findView(scope: ContentDrawScope): View? {
        return reflector.reflect {
            try {
                val drawNode = scope.get<Any>("drawNode") ?: return@reflect null
                val coordinator = drawNode.get<Any>("coordinator") ?: return@reflect null
                val layoutNode = coordinator.get<Any>("layoutNode") ?: return@reflect null
                val interopViewFactoryHolder = layoutNode.get<ViewGroup>("interopViewFactoryHolder") ?: return@reflect null
                interopViewFactoryHolder.getChildAt(0)
            } catch (e: NoSuchFieldException) {
                Logger.e1("Compose14ViewProvider", "findView", e)
                null
            }
        }
    }
}
