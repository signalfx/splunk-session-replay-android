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
