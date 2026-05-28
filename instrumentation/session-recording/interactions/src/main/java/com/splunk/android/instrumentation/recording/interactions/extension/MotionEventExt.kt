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

package com.splunk.android.instrumentation.recording.interactions.extension

import android.view.MotionEvent

internal val MotionEvent.pointerId: Int
    get() = getPointerId(actionIndex)

internal val MotionEvent.actionX: Float
    get() = getX(actionIndex)

internal val MotionEvent.actionY: Float
    get() = getY(actionIndex)

internal val MotionEvent.isTouchUp: Boolean
    get() = action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_UP
