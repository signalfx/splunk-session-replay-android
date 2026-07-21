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

package com.splunk.android.instrumentation.recording.wireframe.identifier

import com.splunk.rum.common.utils.extensions.toClass
import com.splunk.android.instrumentation.recording.wireframe.extension.isInheritedBy
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window

/* TODO
 *  - Stop extraction
 *  - Huawei Map Fragment
 *  - Open street maps
 */
internal class StandardFragmentTypeIdentifier : FragmentTypeIdentifier {

    override fun identify(fragmentClass: Class<out Any>): Window.View.Type? {
        if (GOOGLE_MAPS_PACKAGE?.isInheritedBy(fragmentClass) == true)
            return Window.View.Type.MAP

        return null
    }

    private companion object {
        val GOOGLE_MAPS_PACKAGE = "com.google.android.gms.maps.SupportMapFragment".toClass()
    }
}
