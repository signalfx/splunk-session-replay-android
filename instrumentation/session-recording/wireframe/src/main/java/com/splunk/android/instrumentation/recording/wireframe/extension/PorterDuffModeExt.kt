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

import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

private val CLEAR = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
private val SRC = PorterDuffXfermode(PorterDuff.Mode.SRC)
private val DST = PorterDuffXfermode(PorterDuff.Mode.DST)
private val SRC_OVER = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
private val DST_OVER = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
private val SRC_IN = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
private val DST_IN = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
private val SRC_OUT = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
private val DST_OUT = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
private val SRC_ATOP = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
private val DST_ATOP = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
private val XOR = PorterDuffXfermode(PorterDuff.Mode.XOR)
private val DARKEN = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
private val LIGHTEN = PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
private val MULTIPLY = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
private val SCREEN = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
private val ADD = PorterDuffXfermode(PorterDuff.Mode.ADD)
private val OVERLAY = PorterDuffXfermode(PorterDuff.Mode.OVERLAY)

internal fun PorterDuff.Mode.toPorterDuffXfermode(): PorterDuffXfermode {
    return when (this) {
        PorterDuff.Mode.CLEAR -> CLEAR
        PorterDuff.Mode.SRC -> SRC
        PorterDuff.Mode.DST -> DST
        PorterDuff.Mode.SRC_OVER -> SRC_OVER
        PorterDuff.Mode.DST_OVER -> DST_OVER
        PorterDuff.Mode.SRC_IN -> SRC_IN
        PorterDuff.Mode.DST_IN -> DST_IN
        PorterDuff.Mode.SRC_OUT -> SRC_OUT
        PorterDuff.Mode.DST_OUT -> DST_OUT
        PorterDuff.Mode.SRC_ATOP -> SRC_ATOP
        PorterDuff.Mode.DST_ATOP -> DST_ATOP
        PorterDuff.Mode.XOR -> XOR
        PorterDuff.Mode.DARKEN -> DARKEN
        PorterDuff.Mode.LIGHTEN -> LIGHTEN
        PorterDuff.Mode.MULTIPLY -> MULTIPLY
        PorterDuff.Mode.SCREEN -> SCREEN
        PorterDuff.Mode.ADD -> ADD
        PorterDuff.Mode.OVERLAY -> OVERLAY
    }
}
