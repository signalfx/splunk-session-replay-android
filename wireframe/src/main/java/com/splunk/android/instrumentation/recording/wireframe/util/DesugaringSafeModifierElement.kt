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

import androidx.compose.ui.CombinedModifier
import androidx.compose.ui.Modifier

/**
 * A base class of all [Modifier.Element] implementations of this SDK. It repeats the default implementations of the [Modifier] and
 * [Modifier.Element] interfaces, they are identical to the ones Jetpack Compose declares.
 *
 * Compose is a compileOnly dependency, so D8 does not add the desugared forwarders of the default interface functions into our implementations.
 * Compose calls these functions on every element of a modifier chain, so their absence ends with AbstractMethodError on applications with minSdk
 * lower than 24.
 *
 * This must stay an abstract class.
 */
abstract class DesugaringSafeModifierElement : Modifier.Element {

    override fun then(other: Modifier): Modifier =
        if (other === Modifier) this else CombinedModifier(this, other)

    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
        operation(initial, this)

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R =
        operation(this, initial)

    override fun any(predicate: (Modifier.Element) -> Boolean): Boolean =
        predicate(this)

    override fun all(predicate: (Modifier.Element) -> Boolean): Boolean =
        predicate(this)
}
