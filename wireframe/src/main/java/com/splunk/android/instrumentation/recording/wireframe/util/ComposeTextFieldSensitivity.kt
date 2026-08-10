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

package com.splunk.android.instrumentation.recording.wireframe.util

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collection.MutableVector
import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsModifier
import com.splunk.android.instrumentation.recording.wireframe.WireframeExtractor
import com.splunk.android.instrumentation.recording.wireframe.canvas.compose.SessionReplayDrawModifier
import com.splunk.rum.common.logger.Logger
import com.splunk.rum.common.utils.extensions.findField
import com.splunk.rum.common.utils.extensions.findMethod
import com.splunk.rum.common.utils.extensions.get
import com.splunk.rum.common.utils.extensions.hasField
import com.splunk.rum.common.utils.extensions.toClass
import java.lang.reflect.Field
import java.lang.reflect.Method

// MARK Requires Proguard rules

// TODO Optimization element loop

/**
 * Applies sensitivity defined by `Sensitivity.Compose.textFieldSensitivity` on all Jetpack Compose text fields. The default value of the API
 * makes them sensitive, the same way as [android.widget.EditText] is sensitive in the View world.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
internal object ComposeTextFieldSensitivity {

    private const val TAG = "ComposeTextFieldSensitivity"

    private const val FLAG_INNER_NODE = 1
    private const val FLAG_APPLICATION_SENSITIVITY = 2

    private val sensitiveModifier = SessionReplayDrawModifier(id = null, isSensitive = true)
    private val notSensitiveModifier = SessionReplayDrawModifier(id = null, isSensitive = false)

    private var reflection: ReflectionInterface? = null
    private var isReflectionObtained = false

    fun makeSensitive(rootLayoutNode: Any?) {
        rootLayoutNode ?: return

        val reflection = obtainReflection(rootLayoutNode) ?: return

        if (!reflection.isTextFieldDetectionPossible)
            return

        try {
            makeSensitive(
                reflection = reflection,
                layoutNode = rootLayoutNode,
                requiredModifier = obtainModifier(),
                isInTextField = false,
                isApplicationSensitivityDefinedAbove = false
            )
        } catch (e: ReflectiveOperationException) {
            Logger.e1(TAG, "makeSensitive()", e)
        }
    }

    @SuppressLint("ModifierFactoryExtensionFunction", "ModifierFactoryReturnType")
    private fun obtainModifier(): SessionReplayDrawModifier? {
        return when (WireframeExtractor.sensitivityDeterminer?.isComposeTextFieldSensitive()) {
            true -> sensitiveModifier
            false -> notSensitiveModifier
            else -> null
        }
    }

    /**
     * @param requiredModifier the modifier that text fields must have or null when their sensitivity is not defined.
     * @param isInTextField whether an ancestor is the outer node of a text field.
     * @param isApplicationSensitivityDefinedAbove whether sensitivity of an ancestor is defined by the application. Applied only when the outer
     * node of a text field is not recognized.
     * @return flags of the subtree that are not consumed by the outer node of a text field.
     */
    private fun makeSensitive(
        reflection: ReflectionInterface,
        layoutNode: Any,
        requiredModifier: SessionReplayDrawModifier?,
        isInTextField: Boolean,
        isApplicationSensitivityDefinedAbove: Boolean
    ): Int {
        if (!reflection.isAttached(layoutNode) || reflection.isDeactivated(layoutNode))
            return 0

        val modifier = reflection.getModifier(layoutNode)

        var isInnerNode = false
        var isOuterNode = false
        var isApplicationSensitivityDefined = false
        var appliedModifier: SessionReplayDrawModifier? = null

        if (modifier != null)
            reflection.forEachElement(modifier) { element ->
                when {
                    element is SessionReplayDrawModifier ->
                        if (element.isApplied)
                            appliedModifier = element
                        else if (element.isSensitive != null)
                            isApplicationSensitivityDefined = true
                    reflection.isInnerNodeModifier(element) ->
                        isInnerNode = true
                    reflection.isOuterNodeModifier(element) ->
                        isOuterNode = true
                }
            }

        var subtreeFlags = 0
        val children = reflection.getChildren(layoutNode)

        if (children != null)
            for (i in children.indices)
                subtreeFlags = subtreeFlags or makeSensitive(
                    reflection = reflection,
                    layoutNode = children[i],
                    requiredModifier = requiredModifier,
                    isInTextField = isInTextField || isOuterNode,
                    isApplicationSensitivityDefinedAbove = isApplicationSensitivityDefinedAbove || isApplicationSensitivityDefined
                )

        val isTextField = isOuterNode && subtreeFlags and FLAG_INNER_NODE != 0
        val isUnwrappedTextField = isInnerNode && !isInTextField

        val isSensitivityDefinedInTextField = isApplicationSensitivityDefined || subtreeFlags and FLAG_APPLICATION_SENSITIVITY != 0 || (isUnwrappedTextField && isApplicationSensitivityDefinedAbove)
        val newModifier = if ((isTextField || isUnwrappedTextField) && !isSensitivityDefinedInTextField) requiredModifier else null

        if (newModifier !== appliedModifier && modifier != null)
            reflection.setModifier(layoutNode, reflection.replaceElement(modifier, appliedModifier, newModifier))

        if (isTextField || isUnwrappedTextField)
            return 0

        var flags = subtreeFlags

        if (isInnerNode)
            flags = flags or FLAG_INNER_NODE

        if (isApplicationSensitivityDefined)
            flags = flags or FLAG_APPLICATION_SENSITIVITY

        return flags
    }

    private fun obtainReflection(layoutNode: Any): ReflectionInterface? {
        if (!isReflectionObtained) {
            isReflectionObtained = true
            reflection = ReflectionInterface.create(layoutNode.javaClass)
        }

        return reflection
    }

    private val SessionReplayDrawModifier.isApplied: Boolean
        get() = this === sensitiveModifier || this === notSensitiveModifier

    @SuppressLint("ModifierFactoryExtensionFunction", "ModifierFactoryReturnType")
    private sealed interface ReflectionInterface {

        val isTextFieldDetectionPossible: Boolean

        fun isAttached(layoutNode: Any): Boolean

        fun isDeactivated(layoutNode: Any): Boolean

        fun getModifier(layoutNode: Any): Modifier?

        fun setModifier(layoutNode: Any, modifier: Modifier)

        fun getChildren(layoutNode: Any): MutableVector<Any>?

        fun isInnerNodeModifier(element: Modifier): Boolean

        fun isOuterNodeModifier(element: Modifier): Boolean

        fun forEachElement(modifier: Modifier, action: (Modifier) -> Unit)

        fun replaceElement(modifier: Modifier, oldElement: Modifier?, newElement: Modifier?): Modifier

        private open class V12(layoutNodeClass: Class<*>, modifierFieldName: String = MODIFIER_FIELD) : ReflectionInterface {

            private val modifierField: Field = layoutNodeClass.findField(modifierFieldName)

            private val setModifierMethod: Method = layoutNodeClass.findMethod("setModifier", Void::class.java, Modifier::class.java)
            private val ownerField: Field = layoutNodeClass.findField("owner")

            private val outerField: Field = CombinedModifier::class.java.findField("outer")
            private val innerField: Field = CombinedModifier::class.java.findField("inner")

            protected val foldedChildrenField: Field = layoutNodeClass.findField("_foldedChildren")

            override val isTextFieldDetectionPossible: Boolean
                get() = VERTICAL_SCROLL_LAYOUT_MODIFIER_CLASS != null || HORIZONTAL_SCROLL_LAYOUT_MODIFIER_CLASS != null

            override fun isAttached(layoutNode: Any): Boolean {
                return layoutNode.get<Any>(ownerField) != null
            }

            override fun isDeactivated(layoutNode: Any): Boolean {
                return false
            }

            override fun getModifier(layoutNode: Any): Modifier? {
                return layoutNode.get(modifierField)
            }

            override fun setModifier(layoutNode: Any, modifier: Modifier) {
                setModifierMethod.invoke(layoutNode, modifier)
            }

            override fun getChildren(layoutNode: Any): MutableVector<Any>? {
                return layoutNode.get(foldedChildrenField)
            }

            override fun isInnerNodeModifier(element: Modifier): Boolean {
                val elementClass = element.javaClass
                return elementClass === VERTICAL_SCROLL_LAYOUT_MODIFIER_CLASS || elementClass === HORIZONTAL_SCROLL_LAYOUT_MODIFIER_CLASS
            }

            override fun isOuterNodeModifier(element: Modifier): Boolean {
                if (element.javaClass === SEMANTICS_MODIFIER_CORE_CLASS)
                    return (element as SemanticsModifier).semanticsConfiguration.isMergingSemanticsOfDescendants

                return false
            }

            override fun forEachElement(modifier: Modifier, action: (Modifier) -> Unit) {
                if (modifier is CombinedModifier) {
                    forEachElement(modifier.get(outerField) ?: return, action)
                    forEachElement(modifier.get(innerField) ?: return, action)
                } else
                    action(modifier)
            }

            override fun replaceElement(modifier: Modifier, oldElement: Modifier?, newElement: Modifier?): Modifier {
                val remainingModifier = if (oldElement != null)
                    removeElement(modifier, oldElement)
                else
                    modifier

                return if (newElement != null)
                    CombinedModifier(newElement, remainingModifier)
                else
                    remainingModifier
            }

            private fun removeElement(modifier: Modifier, element: Modifier): Modifier {
                if (modifier === element)
                    return Modifier

                if (modifier !is CombinedModifier)
                    return modifier

                val outer = modifier.get<Modifier>(outerField) ?: return modifier
                val inner = modifier.get<Modifier>(innerField) ?: return modifier

                val newOuter = removeElement(outer, element)
                val newInner = removeElement(inner, element)

                return when {
                    newOuter === outer && newInner === inner -> modifier
                    newOuter === Modifier -> newInner
                    newInner === Modifier -> newOuter
                    else -> CombinedModifier(newOuter, newInner)
                }
            }

            companion object {
                val VERTICAL_SCROLL_LAYOUT_MODIFIER_CLASS = "androidx.compose.foundation.text.VerticalScrollLayoutModifier".toClass()
                val HORIZONTAL_SCROLL_LAYOUT_MODIFIER_CLASS = "androidx.compose.foundation.text.HorizontalScrollLayoutModifier".toClass()
                val SEMANTICS_MODIFIER_CORE_CLASS = "androidx.compose.ui.semantics.SemanticsModifierCore".toClass()
            }
        }

        private open class V13(layoutNodeClass: Class<*>, modifierFieldName: String = MODIFIER_FIELD) : V12(layoutNodeClass, modifierFieldName) {

            private val foldedChildrenVectorField: Field = foldedChildrenField.type.findField("vector")

            override fun getChildren(layoutNode: Any): MutableVector<Any>? {
                return layoutNode.get<Any>(foldedChildrenField)?.get(foldedChildrenVectorField)
            }
        }

        private open class V15(layoutNodeClass: Class<*>, modifierFieldName: String = MODIFIER_FIELD) : V13(layoutNodeClass, modifierFieldName) {

            override fun isOuterNodeModifier(element: Modifier): Boolean {
                if (element.javaClass === APPENDED_SEMANTICS_ELEMENT_CLASS)
                    return MERGE_DESCENDANTS_FIELD?.let { element.get<Boolean>(it) } == true

                return false
            }

            companion object {
                private const val MERGE_DESCENDANTS_FIELD_NAME = "mergeDescendants"

                val APPENDED_SEMANTICS_ELEMENT_CLASS = "androidx.compose.ui.semantics.AppendedSemanticsElement".toClass()

                val MERGE_DESCENDANTS_FIELD = APPENDED_SEMANTICS_ELEMENT_CLASS
                    ?.takeIf { it.hasField(MERGE_DESCENDANTS_FIELD_NAME) }
                    ?.findField(MERGE_DESCENDANTS_FIELD_NAME)
            }
        }

        private open class V16(layoutNodeClass: Class<*>, modifierFieldName: String = MODIFIER_FIELD) : V15(layoutNodeClass, modifierFieldName) {

            private val isDeactivatedField: Field = layoutNodeClass.findField("isDeactivated")

            override fun isDeactivated(layoutNode: Any): Boolean {
                return layoutNode.get(isDeactivatedField) ?: false
            }
        }

        private open class V17(layoutNodeClass: Class<*>) : V16(layoutNodeClass, MODIFIER_FIELD_V1_7) {

            override val isTextFieldDetectionPossible: Boolean
                get() = super.isTextFieldDetectionPossible || TEXT_FIELD_CORE_MODIFIER_CLASS != null

            override fun isInnerNodeModifier(element: Modifier): Boolean {
                return super.isInnerNodeModifier(element) || element.javaClass === TEXT_FIELD_CORE_MODIFIER_CLASS
            }

            override fun isOuterNodeModifier(element: Modifier): Boolean {
                return super.isOuterNodeModifier(element) || element.javaClass === TEXT_FIELD_DECORATOR_MODIFIER_CLASS
            }

            companion object {
                val TEXT_FIELD_CORE_MODIFIER_CLASS = "androidx.compose.foundation.text.input.internal.TextFieldCoreModifier".toClass()
                val TEXT_FIELD_DECORATOR_MODIFIER_CLASS = "androidx.compose.foundation.text.input.internal.TextFieldDecoratorModifier".toClass()
            }
        }

        private class V18(layoutNodeClass: Class<*>) : V17(layoutNodeClass) {

            override fun isOuterNodeModifier(element: Modifier): Boolean {
                return super.isOuterNodeModifier(element) || element.javaClass === CORE_TEXT_FIELD_SEMANTICS_MODIFIER_CLASS
            }

            companion object {
                val CORE_TEXT_FIELD_SEMANTICS_MODIFIER_CLASS = "androidx.compose.foundation.text.input.internal.CoreTextFieldSemanticsModifier".toClass()
            }
        }

        companion object {

            private const val TAG = "ReflectionInterface"

            const val MODIFIER_FIELD = "modifier"
            const val MODIFIER_FIELD_V1_7 = "_modifier"

            fun create(layoutNodeClass: Class<*>): ReflectionInterface? {
                return try {
                    when {
                        ComposeInfo.version >= VERSION_1_8 -> V18(layoutNodeClass)
                        ComposeInfo.version >= VERSION_1_7 -> V17(layoutNodeClass)
                        ComposeInfo.version >= VERSION_1_6 -> V16(layoutNodeClass)
                        ComposeInfo.version >= VERSION_1_5 -> V15(layoutNodeClass)
                        ComposeInfo.version >= VERSION_1_3 -> V13(layoutNodeClass)
                        ComposeInfo.version >= VERSION_1_2 -> V12(layoutNodeClass)
                        else -> {
                            Logger.e1(TAG, "create() - Unknown Compose implementation")
                            null
                        }
                    }
                } catch (e: ReflectiveOperationException) {
                    Logger.e1(TAG, "create()", e)
                    null
                }
            }
        }
    }
}
