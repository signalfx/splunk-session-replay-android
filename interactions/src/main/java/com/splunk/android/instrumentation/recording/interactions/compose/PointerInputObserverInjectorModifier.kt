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

package com.splunk.android.instrumentation.recording.interactions.compose

import android.annotation.SuppressLint
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputFilter
import androidx.compose.ui.input.pointer.PointerInputModifier
import androidx.compose.ui.modifier.ModifierLocalConsumer
import androidx.compose.ui.modifier.ModifierLocalReadScope
import androidx.compose.ui.unit.IntSize
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.anyFast
import com.splunk.rum.common.utils.extensions.findField
import com.splunk.rum.common.utils.extensions.findMethod
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.interactions.extension.composeTargetElementHolder
import com.splunk.android.instrumentation.recording.wireframe.util.ComposeInfo
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_2
import com.splunk.android.instrumentation.recording.wireframe.util.VERSION_1_3
import java.lang.reflect.Field
import java.lang.reflect.Method

// MARK Needs Proguard rules

/**
 * A [Modifier.Element] that injects [PointerInputObserverModifier]s before and after the original
 * modifier chain of a Composable. This is done to observe whether a [PointerEvent] has been
 * consumed by the Composable's pointer input handlers.
 *
 * ### Mechanism
 *
 * This modifier uses reflection to access the internal `LayoutNode` of a Composable and its
 * associated modifier chain. It then wraps the original modifier chain with two instances of
 * [PointerInputObserverModifier], creating a "sandwich" structure: `pre -> original -> post`.
 *
 * 1.  The `pre` observer is the first to receive a [PointerEvent]. It records whether the event has
 *     already been consumed.
 * 2.  The event then passes through the original modifier chain (e.g., `clickable`, `draggable`).
 *     If an interaction occurs, one of these modifiers will consume the event.
 * 3.  Finally, the `post` observer receives the event. It compares the event's current consumed
 *     status with the status recorded by the `pre` observer.
 *
 * If the consumed status has changed from `false` to `true`, it signifies that an interaction
 * has occurred within the original modifier chain. The hash of the element and its position are
 * then stored in the [TargetElementHolder] associated with the owner [View].
 *
 * This reflective approach is necessary due to the lack of public APIs for this purpose and is
 * designed to work across different versions of Jetpack Compose (specifically handling versions
 * 1.2 and 1.3+).
 *
 * @param id A unique identifier for the Composable being observed.
 * @param positionInList The position of the Composable if it's part of a list (e.g., LazyColumn).
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class PointerInputObserverInjectorModifier(
    private val id: String,
    private val positionInList: Int? = null
) : ModifierLocalConsumer {

    override fun onModifierLocalsUpdated(scope: ModifierLocalReadScope) {
        injectObservers(scope)
    }

    /**
     * Core logic for injecting the `pre` and `post` observer modifiers around the original
     * modifier chain using reflection. It ensures that injection only happens once.
     */
    private fun injectObservers(scope: ModifierLocalReadScope) {
        if (!ReflectionInterface.isScopeAllowed(scope))
            return

        val interfaceInstance = ReflectionInterface.instance ?: return
        val modifier = interfaceInstance.getModifier(scope)

        if (modifier is CombinedModifier && modifier.outer is PointerInputObserverModifier)
            return

        val view = interfaceInstance.getOwner(scope)

        var targetElementHolder = view.composeTargetElementHolder

        if (targetElementHolder == null) {
            targetElementHolder = TargetElementHolder(null, null)
            view.composeTargetElementHolder = targetElementHolder
        }

        val elementHash = interfaceInstance.getElementHash(scope)

        val pre = PointerInputObserverModifier(targetElementHolder, id, elementHash, positionInList)
        val post = PointerInputObserverModifier(targetElementHolder, id, elementHash, positionInList)

        pre.pairedObserver = post
        post.pairedObserver = pre

        val combinedModifier = CombinedModifier(pre, CombinedModifier(modifier, post))
        interfaceInstance.setModifier(scope, combinedModifier)
    }

    private val CombinedModifier.outer: Modifier
        get() = get(combinedModifierOuterField) ?: throw NullPointerException()

    private companion object {
        val combinedModifierOuterField: Field = CombinedModifier::class.java.getDeclaredField("outer") // This should exist in all Jetpack Compose versions
    }

    /**
     * A sealed interface that abstracts the reflection calls needed to interact with different internal structures of Jetpack Compose versions.
     */
    @SuppressLint("ModifierFactoryExtensionFunction")
    private sealed interface ReflectionInterface {

        fun getModifier(scope: ModifierLocalReadScope): Modifier

        fun setModifier(scope: ModifierLocalReadScope, modifier: Modifier)

        fun getOwner(scope: ModifierLocalReadScope): View

        fun getElementHash(scope: ModifierLocalReadScope): Int

        /**
         * Reflection implementation for Jetpack Compose version 1.2.x.
         */
        private class V12 : ReflectionInterface {

            private val providerField: Field
            private val layoutNodeField: Field
            private val modifierField: Field
            private val setModifierMethod: Method
            private val ownerField: Field

            init {
                if (modifierLocalConsumerEntityClass == null)
                    throw IllegalStateException("Missing class $modifierLocalConsumerEntityClass")

                providerField = modifierLocalConsumerEntityClass.findField("provider")
                layoutNodeField = providerField.type.findField("layoutNode")
                modifierField = layoutNodeField.type.findField("modifier")
                setModifierMethod = layoutNodeField.type.findMethod("setModifier", Void::class.java, Modifier::class.java)
                ownerField = layoutNodeField.type.findField("owner")
            }

            override fun getModifier(scope: ModifierLocalReadScope): Modifier {
                return getLayoutNode(scope).get(modifierField) ?: throw NullPointerException("No element should be null")
            }

            override fun setModifier(scope: ModifierLocalReadScope, modifier: Modifier) {
                setModifierMethod.invoke(getLayoutNode(scope), modifier)
            }

            override fun getOwner(scope: ModifierLocalReadScope): View {
                val layoutNode = getLayoutNode(scope)
                return ownerField.get(layoutNode) as View
            }

            override fun getElementHash(scope: ModifierLocalReadScope): Int {
                return getLayoutNode(scope).let { System.identityHashCode(it) }
            }

            private fun getLayoutNode(scope: ModifierLocalReadScope): Any {
                return scope.get<Any>(providerField)?.get(layoutNodeField) ?: throw NullPointerException("LayoutNode should not be null")
            }

            companion object {
                val modifierLocalConsumerEntityClass = "androidx.compose.ui.node.ModifierLocalConsumerEntity".toClass()
            }
        }

        /**
         * Reflection implementation for Jetpack Compose version 1.3.x and newer.
         */
        private class V13 : ReflectionInterface {

            private val coordinatorField: Field
            private val layoutNodeField: Field
            private val modifierField: Field
            private val setModifierMethod: Method
            private val ownerField: Field

            init {
                if (backwardsCompatNodeClass == null)
                    throw IllegalStateException("Missing class $backwardsCompatNodeClass")

                coordinatorField = backwardsCompatNodeClass.findField("coordinator")
                layoutNodeField = coordinatorField.type.findField("layoutNode")
                modifierField = runCatching { layoutNodeField.type.findField("_modifier") }.getOrNull() ?: layoutNodeField.type.findField("modifier")
                setModifierMethod = layoutNodeField.type.findMethod("setModifier", Void::class.java, Modifier::class.java)
                ownerField = layoutNodeField.type.findField("owner")
            }

            override fun getModifier(scope: ModifierLocalReadScope): Modifier {
                return getLayoutNode(scope).get(modifierField) ?: throw NullPointerException("Modifier should not be null")
            }

            override fun setModifier(scope: ModifierLocalReadScope, modifier: Modifier) {
                setModifierMethod.invoke(getLayoutNode(scope), modifier)
            }

            override fun getOwner(scope: ModifierLocalReadScope): View {
                return getLayoutNode(scope).get(ownerField) ?: throw NullPointerException("Owner should not be null")
            }

            override fun getElementHash(scope: ModifierLocalReadScope): Int {
                return getLayoutNode(scope).let { System.identityHashCode(it) }
            }

            private fun getLayoutNode(scope: ModifierLocalReadScope): Any {
                return scope.get<Any>(coordinatorField)?.get(layoutNodeField) ?: throw NullPointerException("LayoutNode should not be null")
            }

            companion object {
                val backwardsCompatNodeClass = "androidx.compose.ui.node.BackwardsCompatNode".toClass()
            }
        }

        companion object {

            private const val TAG = "ReflectionInterface"

            @Suppress("JoinDeclarationAndAssignment")
            val instance: ReflectionInterface?

            init {
                instance = try {
                    when {
                        ComposeInfo.version >= VERSION_1_3 -> V13()
                        ComposeInfo.version >= VERSION_1_2 -> V12()
                        else -> {
                            Logger.e1(TAG, "companion.init - Unknown Compose implementation")
                            null
                        }
                    }
                } catch (e: ReflectiveOperationException) {
                    Logger.e1(TAG, "companion.init", e)
                    null
                }
            }

            fun isScopeAllowed(scope: ModifierLocalReadScope): Boolean {
                return scope.javaClass == V13.backwardsCompatNodeClass || scope.javaClass == V12.modifierLocalConsumerEntityClass
            }
        }
    }

    /**
     * An implementation of [PointerInputModifier] that observes pointer events.
     * Two instances of this class are used (a `pre` and a `post` observer) to detect event consumption by comparing their states.
     */
    private class PointerInputObserverModifier(
        private val targetElementHolder: TargetElementHolder,
        private val id: String,
        private val elementHash: Int,
        private val positionInList: Int?
    ) : PointerInputModifier {

        lateinit var pairedObserver: PointerInputObserverModifier

        var isConsumed: Boolean? = null

        override val pointerInputFilter: PointerInputFilter = object : PointerInputFilter() {
            override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) {
                isConsumed = pointerEvent.changes.anyFast { it.isConsumed }

                if (pairedObserver.isConsumed != null) {
                    if (pass == PointerEventPass.Main && isConsumed != pairedObserver.isConsumed) {
                        targetElementHolder.elementHash = elementHash
                        targetElementHolder.positionInList = positionInList
                    }

                    pairedObserver.isConsumed = null
                }
            }

            override fun onCancel() {
                isConsumed = false
            }
        }
    }
}
