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

package com.splunk.android.debugger.util

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import com.splunk.android.common.utils.dpToPxF

internal class TransparentLayerPaint : Paint() {

    init {
        val bitmap = Bitmap.createBitmap(TILE_SIZE.toInt(), TILE_SIZE.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        color = COLOR_LIGHT
        canvas.drawRect(0f, 0f, TILE_HALF_SIZE, TILE_HALF_SIZE, this)
        canvas.drawRect(TILE_HALF_SIZE, TILE_HALF_SIZE, TILE_SIZE, TILE_SIZE, this)

        color = COLOR_DARK
        canvas.drawRect(TILE_HALF_SIZE, 0f, TILE_SIZE, TILE_HALF_SIZE, this)
        canvas.drawRect(0f, TILE_HALF_SIZE, TILE_HALF_SIZE, TILE_SIZE, this)

        shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    private companion object {
        const val COLOR_LIGHT = 0xfff0f0f0.toInt()
        const val COLOR_DARK = 0xffc0c0c0.toInt()

        val TILE_SIZE = dpToPxF(32f)
        val TILE_HALF_SIZE = TILE_SIZE / 2f
    }
}
