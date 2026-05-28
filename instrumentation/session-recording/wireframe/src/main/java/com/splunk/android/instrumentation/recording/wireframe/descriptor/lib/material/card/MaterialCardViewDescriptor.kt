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

package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.card

import android.view.View
import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.cardview.CardViewDescriptor
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* TODO
 *  - Check icon property
 */
internal open class MaterialCardViewDescriptor : CardViewDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.card.MaterialCardView".toClass()

    override fun getForegroundSkeletons(view: View, isSensitive: Boolean, result: MutableList<Window.View.Skeleton>) {}
}
