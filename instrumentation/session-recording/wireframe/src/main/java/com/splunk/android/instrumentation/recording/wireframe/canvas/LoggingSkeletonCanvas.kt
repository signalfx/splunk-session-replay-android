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

package com.splunk.android.instrumentation.recording.wireframe.canvas

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.DrawFilter
import android.graphics.Matrix
import android.graphics.NinePatch
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Picture
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.RenderNode
import android.graphics.fonts.Font
import android.graphics.text.MeasuredText
import android.os.Build
import androidx.annotation.RequiresApi
import com.splunk.rum.common.logger.Logger
import com.splunk.android.instrumentation.recording.wireframe.extension.toPrettyString

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION", "OverridingDeprecatedMember")
internal open class LoggingSkeletonCanvas : SkeletonCanvas {

    constructor() : super() {
        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "constructor()")
    }

    constructor(bitmap: Bitmap) : super(bitmap) {
        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "constructor(bitmap: ${bitmap.toPrettyString()})")
    }

    override fun isHardwareAccelerated(): Boolean {
        val result = super.isHardwareAccelerated()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "isHardwareAccelerated(): $result")

        return result
    }

    override fun setBitmap(bitmap: Bitmap?) {
        super.setBitmap(bitmap)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "setBitmap(bitmap: ${bitmap?.toPrettyString()})")
    }

    override fun enableZ() {
        super.enableZ()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "enableZ()")
    }

    override fun disableZ() {
        super.disableZ()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "disableZ()")
    }

    override fun isOpaque(): Boolean {
        val result = super.isOpaque()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "isOpaque(): $result")

        return result
    }

    override fun getWidth(): Int {
        val result = super.getWidth()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getWidth(): $result")

        return result
    }

    override fun getHeight(): Int {
        val result = super.getHeight()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getHeight(): $result")

        return result
    }

    override fun getDensity(): Int {
        val result = super.getDensity()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getDensity(): $result")

        return result
    }

    override fun setDensity(density: Int) {
        super.setDensity(density)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "setDensity(density: $density)")
    }

    override fun getMaximumBitmapWidth(): Int {
        val result = super.getMaximumBitmapWidth()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getMaximumBitmapWidth(): $result")

        return result
    }

    override fun getMaximumBitmapHeight(): Int {
        val result = super.getMaximumBitmapHeight()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getMaximumBitmapHeight(): $result")

        return result
    }

    override fun save(): Int {
        val result = super.save()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "save(): $result")

        return result
    }

    override fun save(saveFlags: Int): Int {
        val result = super.save()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "save(saveFlags: $saveFlags): $result")

        return result
    }

    override fun saveLayer(bounds: RectF?, paint: Paint?, saveFlags: Int): Int {
        val result = super.saveLayer(bounds, paint, saveFlags)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayer(bounds: $bounds, paint: ${paint?.toPrettyString()}, saveFlags: $saveFlags): $result")

        return result
    }

    override fun saveLayer(bounds: RectF?, paint: Paint?): Int {
        val result = super.saveLayer(bounds, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayer(bounds: $bounds, paint: ${paint?.toPrettyString()}): $result")

        return result
    }

    override fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?, saveFlags: Int): Int {
        val result = super.saveLayer(left, top, right, bottom, paint, saveFlags)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayer(left: $left, top: $top, right: $right, bottom: $bottom, paint: ${paint?.toPrettyString()}, saveFlags: $saveFlags): $result")

        return result
    }

    override fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?): Int {
        val result = super.saveLayer(left, top, right, bottom, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayer(left: $left, top: $top, right: $right, bottom: $bottom, paint: ${paint?.toPrettyString()}): $result")

        return result
    }

    override fun saveLayerAlpha(bounds: RectF?, alpha: Int, saveFlags: Int): Int {
        val result = super.saveLayerAlpha(bounds, alpha, saveFlags)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayerAlpha(bounds: $bounds, alpha: $alpha, saveFlags: $saveFlags): $result")

        return result
    }

    override fun saveLayerAlpha(bounds: RectF?, alpha: Int): Int {
        val result = super.saveLayerAlpha(bounds, alpha)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayerAlpha(bounds: $bounds, alpha: $alpha): $result")

        return result
    }

    override fun saveLayerAlpha(left: Float, top: Float, right: Float, bottom: Float, alpha: Int, saveFlags: Int): Int {
        val result = super.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayerAlpha(left: $left, top: $top, right: $right, bottom: $bottom, alpha: $alpha, saveFlags: $saveFlags): $result")

        return result
    }

    override fun saveLayerAlpha(left: Float, top: Float, right: Float, bottom: Float, alpha: Int): Int {
        val result = super.saveLayerAlpha(left, top, right, bottom, alpha)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "saveLayerAlpha(left: $left, top: $top, right: $right, bottom: $bottom, alpha: $alpha): $result")

        return result
    }

    override fun restore() {
        super.restore()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "restore()")
    }

    override fun getSaveCount(): Int {
        val result = super.getSaveCount()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getSaveCount(): $result")

        return result
    }

    override fun restoreToCount(saveCount: Int) {
        super.restoreToCount(saveCount)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "restoreToCount(saveCount: $saveCount)")
    }

    override fun translate(dx: Float, dy: Float) {
        super.translate(dx, dy)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "translate(dx: $dx, dy: $dy)")
    }

    override fun scale(sx: Float, sy: Float) {
        super.scale(sx, sy)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "scale(sx: $sx, sy: $sy)")
    }

    override fun rotate(degrees: Float) {
        super.rotate(degrees)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "rotate(degrees: $degrees)")
    }

    override fun skew(sx: Float, sy: Float) {
        super.skew(sx, sy)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "skew(sx: $sx, sy: $sy)")
    }

    override fun concat(matrix: Matrix?) {
        super.concat(matrix)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "concat(matrix: $matrix)")
    }

    override fun setMatrix(matrix: Matrix?) {
        super.setMatrix(matrix)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "setMatrix(matrix: $matrix)")
    }

    override fun getMatrix(ctm: Matrix) {
        super.getMatrix(ctm)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getMatrix(ctm: $ctm)")
    }

    override fun clipRect(rect: RectF, op: Region.Op): Boolean {
        val result = super.clipRect(rect, op)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(rect: $rect, op: $op): $result")

        return result
    }

    override fun clipRect(rect: Rect, op: Region.Op): Boolean {
        val result = super.clipRect(rect, op)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(rect: $rect, op: $op): $result")

        return result
    }

    override fun clipRect(rect: RectF): Boolean {
        val result = super.clipRect(rect)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(rect: $rect): $result")

        return result
    }

    override fun clipRect(rect: Rect): Boolean {
        val result = super.clipRect(rect)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(rect: $rect): $result")

        return result
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float, op: Region.Op): Boolean {
        val result = super.clipRect(left, top, right, bottom, op)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(left: $left, top: $top, right: $right, bottom: $bottom, op: $op): $result")

        return result
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        val result = super.clipRect(left, top, right, bottom)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(left: $left, top: $top, right: $right, bottom: $bottom): $result")

        return result
    }

    override fun clipRect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        val result = super.clipRect(left, top, right, bottom)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipRect(left: $left, top: $top, right: $right, bottom: $bottom): $result")

        return result
    }

    override fun clipOutRect(rect: RectF): Boolean {
        val result = super.clipOutRect(rect)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipOutRect(rect: $rect): $result")

        return result
    }

    override fun clipOutRect(rect: Rect): Boolean {
        val result = super.clipOutRect(rect)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipOutRect(rect: $rect): $result")

        return result
    }

    override fun clipOutRect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        val result = super.clipOutRect(left, top, right, bottom)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipOutRect(left: $left, top: $top, right: $right, bottom: $bottom): $result")

        return result
    }

    override fun clipOutRect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        val result = super.clipOutRect(left, top, right, bottom)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipOutRect(left: $left, top: $top, right: $right, bottom: $bottom): $result")

        return result
    }

    override fun clipPath(path: Path, op: Region.Op): Boolean {
        val result = super.clipPath(path, op)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipPath(path: ${path.toPrettyString()}, op: $op): $result")

        return result
    }

    override fun clipPath(path: Path): Boolean {
        val result = super.clipPath(path)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipPath(path: ${path.toPrettyString()}): $result")

        return result
    }

    override fun clipOutPath(path: Path): Boolean {
        val result = super.clipOutPath(path)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "clipOutPath(path: ${path.toPrettyString()}): $result")

        return result
    }

    override fun getDrawFilter(): DrawFilter? {
        val result = super.getDrawFilter()

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getDrawFilter(): $result")

        return result
    }

    override fun setDrawFilter(filter: DrawFilter?) {
        super.setDrawFilter(filter)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "setDrawFilter(filter: $filter)")
    }

    override fun quickReject(rect: RectF, type: EdgeType): Boolean {
        val result = super.quickReject(rect, type)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(rect: $rect, type: $type): $result")

        return result
    }

    override fun quickReject(rect: RectF): Boolean {
        val result = super.quickReject(rect)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(rect: $rect): $result")

        return result
    }

    override fun quickReject(path: Path, type: EdgeType): Boolean {
        val result = super.quickReject(path, type)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(path: ${path.toPrettyString()}, type: $type): $result")

        return result
    }

    override fun quickReject(path: Path): Boolean {
        val result = super.quickReject(path)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(path: ${path.toPrettyString()}): $result")

        return result
    }

    override fun quickReject(left: Float, top: Float, right: Float, bottom: Float, type: EdgeType): Boolean {
        val result = super.quickReject(left, top, right, bottom, type)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(left: $left, top: $top, right: $right, bottom: $bottom, type: $type): $result")

        return result
    }

    override fun quickReject(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        val result = super.quickReject(left, top, right, bottom)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "quickReject(left: $left, top: $top, right: $right, bottom: $bottom): $result")

        return result
    }

    override fun getClipBounds(bounds: Rect?): Boolean {
        val result = super.getClipBounds(bounds)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "getClipBounds(bounds: $bounds): $result")

        return result
    }

    override fun drawPicture(picture: Picture) {
        super.drawPicture(picture)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPicture(picture: $picture)")
    }

    override fun drawPicture(picture: Picture, dst: RectF) {
        super.drawPicture(picture, dst)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPicture(picture: $picture, dst: $dst)")
    }

    override fun drawPicture(picture: Picture, dst: Rect) {
        super.drawPicture(picture, dst)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPicture(picture: $picture, dst: $dst)")
    }

    override fun drawArc(oval: RectF, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
        super.drawArc(oval, startAngle, sweepAngle, useCenter, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawArc(oval: $oval, startAngle: $startAngle, sweepAngle: $sweepAngle, useCenter: $useCenter, paint: ${paint.toPrettyString()})")
    }

    override fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
        super.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawArc(left: $left, top: $top, right: $right, bottom: $bottom, startAngle: $startAngle, sweepAngle: $sweepAngle, useCenter: $useCenter, paint: ${paint.toPrettyString()})")
    }

    override fun drawARGB(a: Int, r: Int, g: Int, b: Int) {
        super.drawARGB(a, r, g, b)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawARGB(a: $a, r: $r, g: $g, b: $b)")
    }

    override fun drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint?) {
        super.drawBitmap(bitmap, left, top, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(bitmap: ${bitmap.toPrettyString()}, left: $left, top: $top, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: RectF, paint: Paint?) {
        super.drawBitmap(bitmap, src, dst, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(bitmap: ${bitmap.toPrettyString()}, src: $src, dst: $dst, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: Rect, paint: Paint?) {
        super.drawBitmap(bitmap, src, dst, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(bitmap: ${bitmap.toPrettyString()}, src: $src, dst: $dst, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmap(colors: IntArray, offset: Int, stride: Int, x: Float, y: Float, width: Int, height: Int, hasAlpha: Boolean, paint: Paint?) {
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(colors: $colors, offset: $offset, stride: $stride, x: $x, y: $y, width: $width, height: $height, hasAlpha: $hasAlpha, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmap(colors: IntArray, offset: Int, stride: Int, x: Int, y: Int, width: Int, height: Int, hasAlpha: Boolean, paint: Paint?) {
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(colors: $colors, offset: $offset, stride: $stride, x: $x, y: $y, width: $width, height: $height, hasAlpha: $hasAlpha, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint?) {
        super.drawBitmap(bitmap, matrix, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmap(bitmap: ${bitmap.toPrettyString()}, matrix: $matrix, paint: ${paint?.toPrettyString()})")
    }

    override fun drawBitmapMesh(bitmap: Bitmap, meshWidth: Int, meshHeight: Int, verts: FloatArray, vertOffset: Int, colors: IntArray?, colorOffset: Int, paint: Paint?) {
        super.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawBitmapMesh(bitmap: ${bitmap.toPrettyString()}, meshWidth: $meshWidth, meshHeight: $meshHeight, verts: $verts, vertOffset: $vertOffset, colors: $colors, colorOffset: $colorOffset, paint: ${paint?.toPrettyString()})")
    }

    override fun drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint) {
        super.drawCircle(cx, cy, radius, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawCircle(cx: $cx, cy: $cy, radius: $radius, paint: ${paint.toPrettyString()})")
    }

    override fun drawColor(color: Int) {
        super.drawColor(color)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawColor(color: $color)")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun drawColor(color: Long) {
        super.drawColor(color)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawColor(color: $color)")
    }

    override fun drawColor(color: Int, mode: PorterDuff.Mode) {
        super.drawColor(color, mode)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawColor(color: $color, mode: $mode)")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawColor(color: Int, mode: BlendMode) {
        super.drawColor(color, mode)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawColor(color: $color, mode: $mode)")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawColor(color: Long, mode: BlendMode) {
        super.drawColor(color, mode)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawColor(color: $color, mode: $mode)")
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: Paint) {
        super.drawLine(startX, startY, stopX, stopY, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawLine(startX: $startX, startY: $startY, stopX: $stopX, stopY: $stopY, paint: ${paint.toPrettyString()})")
    }

    override fun drawLines(pts: FloatArray, offset: Int, count: Int, paint: Paint) {
        super.drawLines(pts, offset, count, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawLines(pts: $pts, offset: $offset, count: $count, paint: ${paint.toPrettyString()})")
    }

    override fun drawLines(pts: FloatArray, paint: Paint) {
        super.drawLines(pts, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawLines(pts: $pts, paint: ${paint.toPrettyString()})")
    }

    override fun drawOval(oval: RectF, paint: Paint) {
        super.drawOval(oval, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawOval(oval: $oval, paint: ${paint.toPrettyString()})")
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        super.drawOval(left, top, right, bottom, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawOval(left: $left, top: $top, right: $right, bottom: $bottom, paint: ${paint.toPrettyString()})")
    }

    override fun drawPaint(paint: Paint) {
        super.drawPaint(paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPaint(paint: ${paint.toPrettyString()})")
    }

    override fun drawPatch(patch: NinePatch, dst: Rect, paint: Paint?) {
        super.drawPatch(patch, dst, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPatch(patch: $patch, dst: $dst, paint: ${paint?.toPrettyString()})")
    }

    override fun drawPatch(patch: NinePatch, dst: RectF, paint: Paint?) {
        super.drawPatch(patch, dst, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPatch(patch: $patch, dst: $dst, paint: ${paint?.toPrettyString()})")
    }

    override fun drawPath(path: Path, paint: Paint) {
        super.drawPath(path, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPath(path: ${path.toPrettyString()}, paint: ${paint.toPrettyString()})")
    }

    override fun drawPoint(x: Float, y: Float, paint: Paint) {
        super.drawPoint(x, y, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPoint(x: $x, y: $y, paint: ${paint.toPrettyString()})")
    }

    override fun drawPoints(pts: FloatArray?, offset: Int, count: Int, paint: Paint) {
        super.drawPoints(pts, offset, count, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPoints(pts: $pts, offset: $offset, count: $count, paint: ${paint.toPrettyString()})")
    }

    override fun drawPoints(pts: FloatArray, paint: Paint) {
        super.drawPoints(pts, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPoints(pts: $pts, paint: ${paint.toPrettyString()})")
    }

    override fun drawPosText(text: CharArray, index: Int, count: Int, pos: FloatArray, paint: Paint) {
        super.drawPosText(text, index, count, pos, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPosText(text: $text, index: $index, count: $count, pos: $pos, paint: ${paint.toPrettyString()})")
    }

    override fun drawPosText(text: String, pos: FloatArray, paint: Paint) {
        super.drawPosText(text, pos, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawPosText(text: $text, pos: $pos, paint: ${paint.toPrettyString()})")
    }

    override fun drawRect(rect: RectF, paint: Paint) {
        super.drawRect(rect, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRect(rect: $rect, paint: ${paint.toPrettyString()})")
    }

    override fun drawRect(r: Rect, paint: Paint) {
        super.drawRect(r, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRect(r: $r, paint: ${paint.toPrettyString()})")
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        super.drawRect(left, top, right, bottom, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRect(left: $left, top: $top, right: $right, bottom: $bottom, paint: ${paint.toPrettyString()})")
    }

    override fun drawRGB(r: Int, g: Int, b: Int) {
        super.drawRGB(r, g, b)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRGB(r: $r, g: $g, b: $b)")
    }

    override fun drawRoundRect(rect: RectF, rx: Float, ry: Float, paint: Paint) {
        super.drawRoundRect(rect, rx, ry, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRoundRect(rect: $rect, rx: $rx, ry: $ry, paint: ${paint.toPrettyString()})")
    }

    override fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, paint: Paint) {
        super.drawRoundRect(left, top, right, bottom, rx, ry, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRoundRect(left: $left, top: $top, right: $right, bottom: $bottom, rx: $rx, ry: $ry, paint: ${paint.toPrettyString()})")
    }

    override fun drawDoubleRoundRect(outer: RectF, outerRx: Float, outerRy: Float, inner: RectF, innerRx: Float, innerRy: Float, paint: Paint) {
        super.drawDoubleRoundRect(outer, outerRx, outerRy, inner, innerRx, innerRy, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawDoubleRoundRect(outer: $outer, outerRx: $outerRx, outerRy: $outerRy, inner: $inner, innerRx: $innerRx, innerRy: $innerRy, paint: ${paint.toPrettyString()})")
    }

    override fun drawDoubleRoundRect(outer: RectF, outerRadii: FloatArray, inner: RectF, innerRadii: FloatArray, paint: Paint) {
        super.drawDoubleRoundRect(outer, outerRadii, inner, innerRadii, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawDoubleRoundRect(outer: $outer, outerRadii: $outerRadii, inner: $inner, innerRadii: $innerRadii, paint: ${paint.toPrettyString()})")
    }

    override fun drawGlyphs(glyphIds: IntArray, glyphIdOffset: Int, positions: FloatArray, positionOffset: Int, glyphCount: Int, font: Font, paint: Paint) {
        super.drawGlyphs(glyphIds, glyphIdOffset, positions, positionOffset, glyphCount, font, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawGlyphs(glyphIds: $glyphIds, glyphIdOffset: $glyphIdOffset, positions: $positions, positionOffset: $positionOffset, glyphCount: $glyphCount, font: $font, paint: ${paint.toPrettyString()})")
    }

    override fun drawText(text: CharArray, index: Int, count: Int, x: Float, y: Float, paint: Paint) {
        super.drawText(text, index, count, x, y, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawText(text: $text, index: $index, count: $count, x: $x, y: $y, paint: ${paint.toPrettyString()})")
    }

    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        super.drawText(text, x, y, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawText(text: $text, x: $x, y: $y, paint: ${paint.toPrettyString()})")
    }

    override fun drawText(text: String, start: Int, end: Int, x: Float, y: Float, paint: Paint) {
        super.drawText(text, start, end, x, y, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawText(text: $text, start: $start, end: $end, x: $x, y: $y, paint: ${paint.toPrettyString()})")
    }

    override fun drawText(text: CharSequence, start: Int, end: Int, x: Float, y: Float, paint: Paint) {
        super.drawText(text, start, end, x, y, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawText(text: $text, start: $start, end: $end, x: $x, y: $y, paint: ${paint.toPrettyString()})")
    }

    override fun drawTextOnPath(text: CharArray, index: Int, count: Int, path: Path, hOffset: Float, vOffset: Float, paint: Paint) {
        super.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawTextOnPath(text: $text, index: $index, count: $count, path: ${path.toPrettyString()}, hOffset: $hOffset, vOffset: $vOffset, paint: ${paint.toPrettyString()})")
    }

    override fun drawTextOnPath(text: String, path: Path, hOffset: Float, vOffset: Float, paint: Paint) {
        super.drawTextOnPath(text, path, hOffset, vOffset, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawTextOnPath(text: $text, path: ${path.toPrettyString()}, hOffset: $hOffset, vOffset: $vOffset, paint: ${paint.toPrettyString()})")
    }

    override fun drawTextRun(text: CharArray, index: Int, count: Int, contextIndex: Int, contextCount: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        super.drawTextRun(text, index, count, contextIndex, contextCount, x, y, isRtl, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawTextRun(text: $text, index: $index, count: $count, contextIndex: $contextIndex, contextCount: $contextCount, x: $x, y: $y, isRtl: $isRtl, paint: ${paint.toPrettyString()})")
    }

    override fun drawTextRun(text: CharSequence, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        super.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawTextRun(text: $text, start: $start, end: $end, contextStart: $contextStart, contextEnd: $contextEnd, x: $x, y: $y, isRtl: $isRtl, paint: ${paint.toPrettyString()})")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawTextRun(text: MeasuredText, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        super.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawTextRun(text: $text, start: $start, end: $end, contextStart: $contextStart, contextEnd: $contextEnd, x: $x, y: $y, isRtl: $isRtl, paint: ${paint.toPrettyString()})")
    }

    override fun drawVertices(mode: VertexMode, vertexCount: Int, verts: FloatArray, vertOffset: Int, texs: FloatArray?, texOffset: Int, colors: IntArray?, colorOffset: Int, indices: ShortArray?, indexOffset: Int, indexCount: Int, paint: Paint) {
        super.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawVertices(mode: $mode, vertexCount: $vertexCount, verts: $verts, vertOffset: $vertOffset, texs: $texs, texOffset: $texOffset, colors: $colors, colorOffset: $colorOffset, indices: $indices, indexOffset: $indexOffset, indexCount: $indexCount, paint: ${paint.toPrettyString()})")
    }

    override fun drawRenderNode(renderNode: RenderNode) {
        super.drawRenderNode(renderNode)

        if (IS_LOGGING_ENABLED)
            Logger.d(TAG, "drawRenderNode(renderNode: $renderNode)")
    }

    private companion object {
        const val IS_LOGGING_ENABLED = true
        const val TAG = "LoggingSkeletonCanvas"
    }
}
