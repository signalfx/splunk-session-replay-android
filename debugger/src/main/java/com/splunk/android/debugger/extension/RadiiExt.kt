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

package com.splunk.android.debugger.extension

import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe

internal fun Wireframe.Frame.Scene.Window.View.Skeleton.Color.Radii.toFloatArray(): FloatArray {
    return floatArrayOf(topLeft.toFloat(), topLeft.toFloat(), topRight.toFloat(), topRight.toFloat(), bottomRight.toFloat(), bottomRight.toFloat(), bottomLeft.toFloat(), bottomLeft.toFloat())
}
