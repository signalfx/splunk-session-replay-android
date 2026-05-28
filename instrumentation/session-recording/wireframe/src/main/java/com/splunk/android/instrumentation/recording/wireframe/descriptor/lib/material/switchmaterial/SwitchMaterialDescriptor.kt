package com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.material.switchmaterial

import com.splunk.android.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.descriptor.lib.appcompat.widget.SwitchCompatDescriptor

internal open class SwitchMaterialDescriptor : SwitchCompatDescriptor() {

    override val intendedClass: Class<*>? = "com.google.android.material.switchmaterial.SwitchMaterial".toClass()
}
