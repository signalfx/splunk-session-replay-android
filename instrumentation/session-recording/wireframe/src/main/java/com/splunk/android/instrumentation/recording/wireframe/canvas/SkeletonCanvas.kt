package com.splunk.android.instrumentation.recording.wireframe.canvas

import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DrawFilter
import android.graphics.Matrix
import android.graphics.Mesh
import android.graphics.NinePatch
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Picture
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.RenderNode
import android.graphics.Typeface
import android.graphics.Xfermode
import android.graphics.fonts.Font
import android.graphics.text.MeasuredText
import android.os.Build
import androidx.annotation.RequiresApi
import com.splunk.android.common.logger.Logger
import com.splunk.android.common.utils.Colors
import com.splunk.android.common.utils.extensions.a
import com.splunk.android.common.utils.extensions.anyFast
import com.splunk.android.common.utils.extensions.b
import com.splunk.android.common.utils.extensions.forEachFast
import com.splunk.android.common.utils.extensions.g
import com.splunk.android.common.utils.extensions.get
import com.splunk.android.common.utils.extensions.r
import com.splunk.android.common.utils.extensions.toRect
import com.splunk.android.common.utils.runOnAndroidAtLeast
import com.splunk.android.instrumentation.recording.wireframe.estimator.BitmapColorsEstimator
import com.splunk.android.instrumentation.recording.wireframe.extension.getDrawColor
import com.splunk.android.instrumentation.recording.wireframe.extension.getFirstNonWhitespaceCharIndex
import com.splunk.android.instrumentation.recording.wireframe.extension.getLastNonWhitespaceCharIndex
import com.splunk.android.instrumentation.recording.wireframe.extension.getLastWhitespaceCharCount
import com.splunk.android.instrumentation.recording.wireframe.extension.getShadowFlag
import com.splunk.android.instrumentation.recording.wireframe.extension.isInside
import com.splunk.android.instrumentation.recording.wireframe.extension.letterSpacingCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.strikeThruPositionCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.strikeThruThicknessCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.toFont
import com.splunk.android.instrumentation.recording.wireframe.extension.toPorterDuffXfermode
import com.splunk.android.instrumentation.recording.wireframe.extension.underlinePositionCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.underlineThicknessCompat
import com.splunk.android.instrumentation.recording.wireframe.extension.wordSpacingCompat
import com.splunk.android.instrumentation.recording.wireframe.model.Wireframe.Frame.Scene.Window.View.Skeleton
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/* FIXME
 *  - clipOutRect
 *  - Region.Op
 *  - Incorrect position with non centered rotation
 *  - drawText of horizontal scrollable centered text (1024 * 1024)
 *  - Inaccurate color estimation for NinePatch
 */
internal open class SkeletonCanvas : Canvas {

    private val states = ArrayList<State>()

    private val matrixValues = FloatArray(9)
    private val tempRect = RectF()
    private val tempPaint = Paint()
    private val tempRadii = Radii(0, 0, 0, 0)

    private var flipHorizontal = false
    private var flipVertical = false
    private var translationX = 0f
    private var translationY = 0f
    private var scaleX = 1f
    private var scaleY = 1f
    private val clipRect = RectF(INFINITE_RECT)
    private val clipRadii = Radii(0, 0, 0, 0)
    private var layerSize = Size(0, 0)

    private val offscreenLayers = ArrayList<Layer>()

    private val actualSkeletons: MutableList<Skeleton>
        get() = offscreenLayers.lastOrNull()?.skeletons ?: skeletons

    open val skeletons: MutableList<Skeleton> = ArrayList()

    var isTextSkeletonsAllowed = false

    constructor() : super()

    constructor(bitmap: Bitmap) : super() {
        clipRect.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        layerSize = Size(bitmap.width, bitmap.height)
    }

    init {
        states += State(false, false, 0f, 0f, 1f, 1f, INFINITE_RECT, Radii(0))
    }

    override fun setBitmap(bitmap: Bitmap?) {
        if (bitmap != null) {
            clipRect.set(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            layerSize = Size(bitmap.width, bitmap.height)
        } else {
            clipRect.set(0f, 0f, 0f, 0f)
            layerSize = Size(0, 0)
        }

        clipRadii.set(0)
    }

    override fun getWidth(): Int {
        return layerSize.width
    }

    override fun getHeight(): Int {
        return layerSize.height
    }

    override fun translate(dx: Float, dy: Float) {
        translateInternal(dx, dy)
    }

    override fun scale(sx: Float, sy: Float) {
        scaleInternal(sx, sy)
    }

    override fun rotate(degrees: Float) {} // TODO

    override fun skew(sx: Float, sy: Float) {} // TODO

    override fun setMatrix(matrix: Matrix?) {
        setMatrixInternal(matrix)
    }

    override fun setDrawFilter(filter: DrawFilter?) {} // TODO

    override fun concat(matrix: Matrix?) {
        concatInternal(matrix)
    }

    override fun save(): Int {
        return saveInternal()
    }

    // This is method override of @removed method in Java.
    @Suppress("unused")
    open fun save(saveFlags: Int): Int {
        return saveInternal()
    }

    // This is method override of @hide method in Java
    @Suppress("unused")
    open fun saveUnclippedLayer(left: Int, top: Int, right: Int, bottom: Int): Int {
        return saveInternal()
    }

    // This is method override of @hide method in Java
    @Suppress("unused")
    open fun restoreUnclippedLayer(saveCount: Int, paint: Paint) {
        restoreToCountInternal(saveCount)
    }

    @Deprecated("Deprecated in Java")
    override fun saveLayer(bounds: RectF?, paint: Paint?, saveFlags: Int): Int {
        return saveLayerInternal(bounds, paint?.xfermode, paint?.colorFilter, paint?.alpha)
    }

    override fun saveLayer(bounds: RectF?, paint: Paint?): Int {
        return saveLayerInternal(bounds, paint?.xfermode, paint?.colorFilter, paint?.alpha)
    }

    @Deprecated("Deprecated in Java")
    override fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?, saveFlags: Int): Int {
        return saveLayerInternal(left, top, right, bottom, paint?.xfermode, paint?.colorFilter, paint?.alpha)
    }

    override fun saveLayer(left: Float, top: Float, right: Float, bottom: Float, paint: Paint?): Int {
        return saveLayerInternal(left, top, right, bottom, paint?.xfermode, paint?.colorFilter, paint?.alpha)
    }

    @Deprecated("Deprecated in Java")
    override fun saveLayerAlpha(bounds: RectF?, alpha: Int, saveFlags: Int): Int {
        return saveLayerInternal(bounds, null, null, alpha)
    }

    override fun saveLayerAlpha(bounds: RectF?, alpha: Int): Int {
        return saveLayerInternal(bounds, null, null, alpha)
    }

    @Deprecated("Deprecated in Java")
    override fun saveLayerAlpha(left: Float, top: Float, right: Float, bottom: Float, alpha: Int, saveFlags: Int): Int {
        return saveLayerInternal(left, top, right, bottom, null, null, alpha)
    }

    override fun saveLayerAlpha(left: Float, top: Float, right: Float, bottom: Float, alpha: Int): Int {
        return saveLayerInternal(left, top, right, bottom, null, null, alpha)
    }

    override fun getSaveCount(): Int {
        return states.size
    }

    override fun restore() {
        restoreToCountInternal(saveCount - 1)
    }

    override fun restoreToCount(saveCount: Int) {
        restoreToCountInternal(saveCount)
    }

    @Deprecated("Deprecated in Java")
    override fun clipRect(rect: RectF, op: Region.Op): Boolean {
        return when (op) {
            Region.Op.INTERSECT -> clipRectInternal(rect.left, rect.top, rect.right, rect.bottom, null)
            Region.Op.DIFFERENCE -> clipOutRectInternal(rect.left, rect.top, rect.right, rect.bottom)
            else -> true
        }
    }

    @Deprecated("Deprecated in Java")
    override fun clipRect(rect: Rect, op: Region.Op): Boolean {
        return when (op) {
            Region.Op.INTERSECT -> clipRectInternal(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), null)
            Region.Op.DIFFERENCE -> clipOutRectInternal(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
            else -> true
        }
    }

    override fun clipRect(rect: RectF): Boolean {
        return clipRectInternal(rect.left, rect.top, rect.right, rect.bottom, null)
    }

    override fun clipRect(rect: Rect): Boolean {
        return clipRectInternal(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat(), null)
    }

    @Deprecated("Deprecated in Java")
    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float, op: Region.Op): Boolean {
        return when (op) {
            Region.Op.INTERSECT -> clipRectInternal(left, top, right, bottom, null)
            Region.Op.DIFFERENCE -> clipOutRectInternal(left, top, right, bottom)
            else -> true
        }
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return clipRectInternal(left, top, right, bottom, null)
    }

    override fun clipRect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return clipRectInternal(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), null)
    }

    @Deprecated("Deprecated in Java")
    override fun clipPath(path: Path, op: Region.Op): Boolean {
        return when (op) {
            Region.Op.INTERSECT -> clipPathInternal(path)
            Region.Op.DIFFERENCE -> clipOutPathInternal(path)
            else -> true
        }
    }

    override fun clipPath(path: Path): Boolean {
        return clipPathInternal(path)
    }

    /* Method override of Java's getClipBounds(Rect).
     * In SDK < 33 parameter bounds was @Nullable and in SDK >= 33 is @NonNull. It causes an issue because the function can still be called with null in applications
     * compiled with SDK < 33 but also with SDK >= 33, because of calling method with value null for @NonNull parameter does not cause compilation error in Java.
     * Kotlin compiler argument is used to ignore annotations from android.annotation package. The code can be compiled but IDE shows error. It is OK.
     */
    override fun getClipBounds(bounds: Rect?): Boolean {
        return getClipBoundsInternal(bounds)
    }

    override fun clipOutRect(rect: RectF): Boolean {
        return clipOutRectInternal(rect.left, rect.top, rect.right, rect.bottom)
    }

    override fun clipOutRect(rect: Rect): Boolean {
        return clipOutRectInternal(rect.left.toFloat(), rect.top.toFloat(), rect.right.toFloat(), rect.bottom.toFloat())
    }

    override fun clipOutRect(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return clipOutRectInternal(left, top, right, bottom)
    }

    override fun clipOutRect(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return clipOutRectInternal(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    override fun clipOutPath(path: Path): Boolean {
        return clipOutPathInternal(path)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun quickReject(rect: RectF, type: EdgeType): Boolean {
        return quickRejectInternal(rect.left, rect.top, rect.right, rect.bottom)
    }

    override fun quickReject(rect: RectF): Boolean {
        return quickRejectInternal(rect.left, rect.top, rect.right, rect.bottom)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun quickReject(path: Path, type: EdgeType): Boolean {
        return quickRejectInternal(path)
    }

    override fun quickReject(path: Path): Boolean {
        return quickRejectInternal(path)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun quickReject(left: Float, top: Float, right: Float, bottom: Float, type: EdgeType): Boolean {
        return quickRejectInternal(left, top, right, bottom)
    }

    override fun quickReject(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        return quickRejectInternal(left, top, right, bottom)
    }

    override fun drawText(text: CharArray, index: Int, count: Int, x: Float, y: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, index, count, x, y, paint)
        }
    }

    override fun drawText(text: String, x: Float, y: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, 0, text.length, x, y, paint)
        }
    }

    override fun drawText(text: String, start: Int, end: Int, x: Float, y: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, start, end, x, y, paint)
        }
    }

    override fun drawText(text: CharSequence, start: Int, end: Int, x: Float, y: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, start, end, x, y, paint)
        }
    }

    override fun drawTextRun(text: CharArray, index: Int, count: Int, contextIndex: Int, contextCount: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, index, count, x, y, paint)
        }
    }

    override fun drawTextRun(text: CharSequence, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        runIfSpaceAvailable {
            drawTextInternal(text, start, end, x, y, paint)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawTextRun(text: MeasuredText, start: Int, end: Int, contextStart: Int, contextEnd: Int, x: Float, y: Float, isRtl: Boolean, paint: Paint) {
        runIfSpaceAvailable {
            val textWidth = text.getWidth(start, end)
            drawTextInternal("", 0, 0, x, y, x, textWidth, paint, false) // FIXME Use HiddenApiBypass to get characters
        }
    }

    override fun drawCircle(cx: Float, cy: Float, radius: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawCircleInternal(cx, cy, radius, paint)
        }
    }

    override fun drawRect(rect: RectF, paint: Paint) {
        runIfSpaceAvailable {
            drawRectInternal(rect.left, rect.top, rect.right, rect.bottom, null, paint, true)
        }
    }

    override fun drawRect(r: Rect, paint: Paint) {
        runIfSpaceAvailable {
            drawRectInternal(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat(), null, paint, true)
        }
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawRectInternal(left, top, right, bottom, null, paint, true)
        }
    }

    override fun drawRoundRect(rect: RectF, rx: Float, ry: Float, paint: Paint) {
        runIfSpaceAvailable {
            val radius = min(rx, ry).roundToInt()

            val radii = if (radius != 0)
                tempRadii.set(radius)
            else
                null

            drawRectInternal(rect.left, rect.top, rect.right, rect.bottom, radii, paint, false)
        }
    }

    override fun drawRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float, paint: Paint) {
        runIfSpaceAvailable {
            var radius = min(rx, ry).roundToInt()

            if (paint.style == Paint.Style.STROKE || paint.style == Paint.Style.FILL_AND_STROKE)
                radius += (paint.strokeWidth / 2).toInt()

            val radii = if (radius != 0)
                tempRadii.set(radius)
            else
                null

            drawRectInternal(left, top, right, bottom, radii, paint, false)
        }
    }

    override fun drawBitmap(bitmap: Bitmap, left: Float, top: Float, paint: Paint?) {
        runIfSpaceAvailable {
            drawBitmapInternal(bitmap, 0, 0, bitmap.width, bitmap.height, 0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)
        }
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: RectF, paint: Paint?) {
        runIfSpaceAvailable {
            drawBitmapInternal(bitmap, src, dst.left, dst.top, dst.right, dst.bottom, paint)
        }
    }

    override fun drawBitmap(bitmap: Bitmap, src: Rect?, dst: Rect, paint: Paint?) {
        runIfSpaceAvailable {
            drawBitmapInternal(bitmap, src, dst.left.toFloat(), dst.top.toFloat(), dst.right.toFloat(), dst.bottom.toFloat(), paint)
        }
    }

    override fun drawBitmap(bitmap: Bitmap, matrix: Matrix, paint: Paint?) {
        runIfSpaceAvailable {
            drawBitmapInternal(bitmap, matrix, paint)
        }
    }

    override fun drawOval(oval: RectF, paint: Paint) {
        runIfSpaceAvailable {
            drawRectInternal(oval.left, oval.top, oval.right, oval.bottom, null, paint, false)
        }
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawRectInternal(left, top, right, bottom, null, paint, false)
        }
    }

    override fun drawArc(oval: RectF, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
        runIfSpaceAvailable {
            drawArcInternal(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint)
        }
    }

    override fun drawArc(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
        runIfSpaceAvailable {
            drawArcInternal(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint)
        }
    }

    override fun drawPath(path: Path, paint: Paint) {
        runIfSpaceAvailable {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && decompileRoundedRect(path, tempRect, tempRadii))
                drawRectInternal(
                    left = tempRect.left,
                    top = tempRect.top,
                    right = tempRect.right,
                    bottom = tempRect.bottom,
                    radii = tempRadii,
                    colors = Colors(paint.getDrawColor()),
                    flags = paint.getShadowFlag()?.let { Skeleton.Color.Flags(it) },
                    isFullyCovered = false
                )
            else
                drawPathInternal(path, paint)
        }
    }

    override fun drawColor(color: Int) {
        runIfSpaceAvailable {
            drawColorInternal(color, PorterDuff.Mode.SRC_OVER)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun drawColor(color: Long) {
        runIfSpaceAvailable {
            drawColorInternal(Color.toArgb(color), PorterDuff.Mode.SRC_OVER)
        }
    }

    override fun drawColor(color: Int, mode: PorterDuff.Mode) {
        runIfSpaceAvailable {
            drawColorInternal(color, mode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawColor(color: Int, mode: BlendMode) {
        runIfSpaceAvailable {
            drawColorInternal(color, mode)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun drawColor(color: Long, mode: BlendMode) {
        runIfSpaceAvailable {
            drawColorInternal(Color.toArgb(color), mode)
        }
    }

    override fun drawRGB(r: Int, g: Int, b: Int) {
        runIfSpaceAvailable {
            drawColorInternal(Color.argb(255, r, g, b), PorterDuff.Mode.SRC_OVER)
        }
    }

    override fun drawARGB(a: Int, r: Int, g: Int, b: Int) {
        runIfSpaceAvailable {
            drawColorInternal(Color.argb(a, r, g, b), PorterDuff.Mode.SRC_OVER)
        }
    }

    override fun drawPaint(paint: Paint) {
        runIfSpaceAvailable {
            drawPaintInternal(paint)
        }
    }

    override fun drawLine(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: Paint) {
        runIfSpaceAvailable {
            drawLineInternal(startX, startY, stopX, stopY, paint)
        }
    }

    override fun drawLines(pts: FloatArray, offset: Int, count: Int, paint: Paint) {
        runIfSpaceAvailable {
            drawLinesInternal(pts, offset, count, paint)
        }
    }

    override fun drawLines(pts: FloatArray, paint: Paint) {
        runIfSpaceAvailable {
            drawLinesInternal(pts, 0, pts.size, paint)
        }
    }

    override fun drawPatch(patch: NinePatch, dst: Rect, paint: Paint?) {
        runIfSpaceAvailable {
            drawPatchInternal(patch, dst.left.toFloat(), dst.top.toFloat(), dst.right.toFloat(), dst.bottom.toFloat(), paint)
        }
    }

    override fun drawPatch(patch: NinePatch, dst: RectF, paint: Paint?) {
        runIfSpaceAvailable {
            drawPatchInternal(patch, dst.left, dst.top, dst.right, dst.bottom, paint)
        }
    }

    override fun drawPoint(x: Float, y: Float, paint: Paint) {
        Logger.w1(TAG, "drawPoint(Float, Float, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawPoints(pts: FloatArray?, offset: Int, count: Int, paint: Paint) {
        Logger.w1(TAG, "drawPoints(FloatArray?, Int, Int, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawPoints(pts: FloatArray, paint: Paint) {
        Logger.w1(TAG, "drawPoints(FloatArray, Paint) - NOT SUPPORTED") // TODO
    }

    @Deprecated("Deprecated in Java")
    override fun drawPosText(text: CharArray, index: Int, count: Int, pos: FloatArray, paint: Paint) {
        Logger.w1(TAG, "drawPosText(CharArray, Int, Int, FloatArray, Paint) - NOT SUPPORTED") // TODO
    }

    @Deprecated("Deprecated in Java")
    override fun drawPosText(text: String, pos: FloatArray, paint: Paint) {
        Logger.w1(TAG, "drawPosText(String, FloatArray, Paint) - NOT SUPPORTED") // TODO
    }

    @Deprecated("Deprecated in Java")
    override fun drawBitmap(colors: IntArray, offset: Int, stride: Int, x: Float, y: Float, width: Int, height: Int, hasAlpha: Boolean, paint: Paint?) {
        Logger.w1(TAG, "drawBitmap(IntArray, Int, Int, Float, Float, Int, Int, Boolean, Paint?) - NOT SUPPORTED") // TODO
    }

    @Deprecated("Deprecated in Java")
    override fun drawBitmap(colors: IntArray, offset: Int, stride: Int, x: Int, y: Int, width: Int, height: Int, hasAlpha: Boolean, paint: Paint?) {
        Logger.w1(TAG, "drawBitmap(IntArray, Int, Int, Int, Int, Int, Int, Boolean, Paint?) - NOT SUPPORTED") // TODO
    }

    override fun drawDoubleRoundRect(outer: RectF, outerRx: Float, outerRy: Float, inner: RectF, innerRx: Float, innerRy: Float, paint: Paint) {
        Logger.w1(TAG, "drawDoubleRoundRect(RectF, Float, Float, RectF, Float, Float, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawDoubleRoundRect(outer: RectF, outerRadii: FloatArray, inner: RectF, innerRadii: FloatArray, paint: Paint) {
        Logger.w1(TAG, "drawDoubleRoundRect(RectF, FloatArray, RectF, FloatArray, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawGlyphs(glyphIds: IntArray, glyphIdOffset: Int, positions: FloatArray, positionOffset: Int, glyphCount: Int, font: Font, paint: Paint) {
        Logger.w1(TAG, "drawGlyphs(IntArray, Int, FloatArray, Int, Int, Font, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawTextOnPath(text: CharArray, index: Int, count: Int, path: Path, hOffset: Float, vOffset: Float, paint: Paint) {
        Logger.w1(TAG, "drawTextOnPath(CharArray, Int, Int, Path, Float, Float, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawTextOnPath(text: String, path: Path, hOffset: Float, vOffset: Float, paint: Paint) {
        Logger.w1(TAG, "drawTextOnPath(String, Path, Float, Float, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawVertices(mode: VertexMode, vertexCount: Int, verts: FloatArray, vertOffset: Int, texs: FloatArray?, texOffset: Int, colors: IntArray?, colorOffset: Int, indices: ShortArray?, indexOffset: Int, indexCount: Int, paint: Paint) {
        Logger.w1(TAG, "drawVertices(VertexMode, Int, FloatArray, Int, FloatArray?, Int, IntArray?, Int, ShortArray?, Int, Int, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawRenderNode(renderNode: RenderNode) {
        Logger.w1(TAG, "drawRenderNode(RenderNode) - NOT SUPPORTED") // TODO
    }

    override fun drawBitmapMesh(bitmap: Bitmap, meshWidth: Int, meshHeight: Int, verts: FloatArray, vertOffset: Int, colors: IntArray?, colorOffset: Int, paint: Paint?) {
        Logger.w1(TAG, "drawBitmapMesh(Bitmap, Int, Int, FloatArray, Int, IntArray?, Int, Paint?) - NOT SUPPORTED") // TODO
    }

    override fun drawMesh(mesh: Mesh, blendMode: BlendMode?, paint: Paint) {
        Logger.w1(TAG, "drawMesh(Mesh, BlendMode?, Paint) - NOT SUPPORTED") // TODO
    }

    override fun drawPicture(picture: Picture) {
        Logger.w1(TAG, "drawPicture(Picture) - NOT SUPPORTED") // TODO
    }

    override fun drawPicture(picture: Picture, dst: RectF) {
        Logger.w1(TAG, "drawPicture(Picture, RectF) - NOT SUPPORTED") // TODO
    }

    override fun drawPicture(picture: Picture, dst: Rect) {
        Logger.w1(TAG, "drawPicture(Picture, Rect) - NOT SUPPORTED") // TODO
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun decompileRoundedRect(path: Path, resultRect: RectF, resultRadii: Radii): Boolean { // FIXME Case when element with corner is partially covered with another element.
        val points = path.approximate(1f)
        val lines = ArrayList<Line>(4)

        var lastX = Float.POSITIVE_INFINITY
        var lastY = Float.POSITIVE_INFINITY

        var minX = Float.POSITIVE_INFINITY
        var maxX = Float.NEGATIVE_INFINITY
        var minY = Float.POSITIVE_INFINITY
        var maxY = Float.NEGATIVE_INFINITY

        for (i in points.indices step 3) {
            val x = points[i + 1]
            val y = points[i + 2]

            if (x > maxX)
                maxX = x

            if (x < minX)
                minX = x

            if (y > maxY)
                maxY = y

            if (y < minY)
                minY = y

            if (x == lastX || y == lastY)
                lines += Line(lastX, lastY, x, y)

            lastX = x
            lastY = y
        }

        if (lines.isEmpty()) { // Potential circle
            if (minX != 0f || minY != 0f || maxX != maxY)
                return false

            val pointCount = (points.size - 1) / 3
            val segmentPointCount = pointCount / 4

            if ((points[segmentPointCount * 3 * 1] * 100).roundToInt() != 25 || (points[segmentPointCount * 3 * 2] * 100).roundToInt() != 50 || (points[segmentPointCount * 3 * 3] * 100).roundToInt() != 75)
                return false

            if (points[1] != minX || points[2] != maxY / 2 || points[segmentPointCount * 3 * 1 + 1] != maxX / 2 || points[segmentPointCount * 3 * 1 + 2] != maxY || points[segmentPointCount * 3 * 2 + 1] != maxX || points[segmentPointCount * 3 * 2 + 2] != maxY / 2 || points[segmentPointCount * 3 * 3 + 1] != maxX / 2 || points[segmentPointCount * 3 * 3 + 2] != minY)
                return false

            val radius = (maxX / 2).toInt()

            resultRadii.topLeft = radius
            resultRadii.topRight = radius
            resultRadii.bottomLeft = radius
            resultRadii.bottomRight = radius

            resultRect.left = minX
            resultRect.top = minY
            resultRect.right = maxX
            resultRect.bottom = maxY

            return true
        }

        if (lines.size == 4) { // Potential rounded rectangle
            if (lines.anyFast { it.startX != minX && it.startX != maxX && it.startY != minY && it.startY != maxY })
                return false

            val leftLine = lines[0]
            val bottomLine = lines[1]
            val rightLine = lines[2]
            val topLine = lines[3]

            resultRadii.bottomLeft = min(bottomLine.startX - leftLine.endX, bottomLine.startY - leftLine.endY).roundToInt()
            resultRadii.bottomRight = min(rightLine.startX - bottomLine.endX, bottomLine.endY - rightLine.startY).roundToInt()
            resultRadii.topRight = min(rightLine.endX - topLine.startX, rightLine.endY - topLine.startY).roundToInt()
            resultRadii.topLeft = min(topLine.startX - leftLine.startX, leftLine.startY - topLine.endY).roundToInt()

            resultRect.left = minX
            resultRect.top = minY
            resultRect.right = maxX
            resultRect.bottom = maxY

            return true
        }

        return false
    }

    private fun translateInternal(dx: Float, dy: Float) {
        translationX += dx * scaleX
        translationY += dy * scaleY
    }

    private fun scaleInternal(sx: Float, sy: Float) {
        if (sx < 0f)
            flipHorizontal = !flipHorizontal

        if (sy < 0f)
            flipVertical = !flipVertical

        scaleX *= abs(sx)
        scaleY *= abs(sy)
    }

    private fun setMatrixInternal(matrix: Matrix?) {
        if (matrix == null) {
            flipHorizontal = false
            flipVertical = false
            translationX = 0f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
        } else {
            matrix.getValues(matrixValues)
            flipHorizontal = matrixValues[0] < 0f
            flipVertical = matrixValues[4] < 0f
            translationX = matrixValues[2] * scaleX
            translationY = matrixValues[5] * scaleY
            scaleX = abs(matrixValues[0])
            scaleY = abs(matrixValues[4])
        }
    }

    private fun concatInternal(matrix: Matrix?) {
        if (matrix == null)
            return

        matrix.getValues(matrixValues)
        translationX += matrixValues[2] * scaleX
        translationY += matrixValues[5] * scaleY
        scaleX *= abs(matrixValues[0])
        scaleY *= abs(matrixValues[4])

        if (matrixValues[0] < 0f)
            flipHorizontal = !flipHorizontal

        if (matrixValues[4] < 0f)
            flipVertical = !flipVertical
    }

    private fun saveInternal(): Int {
        states += State(flipHorizontal, flipVertical, translationX, translationY, scaleX, scaleY, RectF(clipRect), Radii(clipRadii))
        return states.lastIndex
    }

    private fun saveLayerInternal(bounds: RectF?, xfermode: Xfermode?, colorFilter: ColorFilter?, alpha: Int?): Int {
        return if (bounds == null)
            saveLayerInternal(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom, xfermode, colorFilter, alpha)
        else
            saveLayerInternal(bounds.left, bounds.top, bounds.right, bounds.bottom, xfermode, colorFilter, alpha)
    }

    private fun saveLayerInternal(left: Float, top: Float, right: Float, bottom: Float, xfermode: Xfermode?, colorFilter: ColorFilter?, alpha: Int?): Int {
        val saveCount = saveInternal()
        offscreenLayers += Layer(saveCount, RectF(left, top, right, bottom), colorFilter, xfermode, alpha)
        return saveCount
    }

    @Suppress("NAME_SHADOWING")
    private fun restoreToCountInternal(saveCount: Int) {
        val saveCount = if (saveCount < 1) 1 else saveCount

        if (saveCount > states.lastIndex)
            return

        for (i in offscreenLayers.indices.reversed()) {
            val layer = offscreenLayers[i]

            if (layer.saveCount < saveCount)
                break

            offscreenLayers.removeAt(i)

            if (layer.alpha == 0)
                continue

            val alpha = (layer.alpha ?: 255) / 255f

            // FIXME xfermode, colorFilter, bounds

            layer.skeletons.forEachFast { skeleton ->
                skeletons += when (skeleton) {
                    is Skeleton.Color -> {
                        skeleton.copy(
                            colors = skeleton.colors * alpha,
                            isOpaque = alpha == 1f
                        )
                    }
                    is Skeleton.Text -> {
                        skeleton.copy(
                            color = Color.argb((skeleton.color.a * alpha).toInt(), skeleton.color.r, skeleton.color.g, skeleton.color.b)
                        )
                    }
                }
            }
        }

        val state = states[saveCount]

        flipHorizontal = state.flipHorizontal
        flipVertical = state.flipVertical
        translationX = state.translationX
        translationY = state.translationY
        scaleX = state.scaleX
        scaleY = state.scaleY
        clipRect.set(state.clipRect)
        clipRadii.set(state.clipRadii)

        for (i in (saveCount until states.size).reversed())
            states.removeAt(i)
    }

    private fun clipRectInternal(left: Float, top: Float, right: Float, bottom: Float, radii: Radii?): Boolean {
        tempRect.set(clipRect)

        val intersect = clipRect.intersect(translationX + left, translationY + top, translationX + right, translationY + bottom)

        if (intersect) {
            if (radii == null) {
                val leftDiff = clipRect.left - tempRect.left
                val topDiff = clipRect.top - tempRect.top
                val rightDiff = tempRect.right - clipRect.right
                val bottomDiff = tempRect.bottom - clipRect.bottom

                if (leftDiff != 0f || topDiff != 0f)
                    clipRadii.topLeft = 0

                if (topDiff != 0f || rightDiff != 0f)
                    clipRadii.topRight = 0

                if (bottomDiff != 0f || rightDiff != 0f)
                    clipRadii.bottomRight = 0

                if (bottomDiff != 0f || leftDiff != 0f)
                    clipRadii.bottomLeft = 0
            } else {
                clipRadii.topLeft = max(clipRadii.topLeft, radii.topLeft)
                clipRadii.topRight = max(clipRadii.topRight, radii.topRight)
                clipRadii.bottomRight = max(clipRadii.bottomRight, radii.bottomRight)
                clipRadii.bottomLeft = max(clipRadii.bottomLeft, radii.bottomLeft)
            }
        } else {
            clipRect.set(0f, 0f, 0f, 0f)
            clipRadii.set(0)
        }

        return intersect
    }

    private fun clipPathInternal(path: Path): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && decompileRoundedRect(path, tempRect, tempRadii))
            return clipRectInternal(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom, tempRadii)
        else {
            path.computeBounds(tempRect, true)
            return clipRectInternal(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom, null)
        }
    }

    private fun getClipBoundsInternal(bounds: Rect?): Boolean {
        if (bounds != null) {
            bounds.set(clipRect.left.toInt(), clipRect.top.toInt(), clipRect.right.toInt(), clipRect.bottom.toInt())
            bounds.offset(-translationX.toInt(), -translationY.toInt())
        }

        return !clipRect.isEmpty
    }

    private fun clipOutRectInternal(left: Float, top: Float, right: Float, bottom: Float): Boolean { // TODO
        return true
    }

    private fun clipOutPathInternal(path: Path): Boolean { // TODO
        return true
    }

    private fun quickRejectInternal(path: Path): Boolean {
        path.computeBounds(tempRect, true)
        return quickRejectInternal(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom)
    }

    private fun quickRejectInternal(left: Float, top: Float, right: Float, bottom: Float): Boolean {
        tempRect.set(left, top, right, bottom)
        tempRect.offset(translationX, translationY)
        return !RectF.intersects(clipRect, tempRect)
    }

    private fun drawCircleInternal(cx: Float, cy: Float, radius: Float, paint: Paint) {
        if (radius <= 0f)
            return

        val left = cx - radius
        val top = cy - radius
        val right = cx + radius
        val bottom = cy + radius

        tempRadii.set(radius.roundToInt())

        drawRectInternal(left, top, right, bottom, tempRadii, paint, false)
    }

    private fun drawTextInternal(text: CharArray, index: Int, count: Int, x: Float, y: Float, paint: Paint) {
        val visibleIndex: Int
        val visibleCount: Int

        if (paint.isUnderlineText || paint.isStrikeThruText) {
            visibleIndex = index
            visibleCount = count
        } else {
            visibleIndex = text.getFirstNonWhitespaceCharIndex(index, count) ?: return
            visibleCount = count - (visibleIndex - index) - text.getLastWhitespaceCharCount(index, count)
        }

        val textWidth = paint.measureText(text, index, count)
        val invisiblePrefixWidth = if (visibleIndex != index) paint.measureText(text, index, visibleIndex - index) else 0f
        val invisiblePostfixWidth = if (visibleCount != count) paint.measureText(text, visibleIndex + visibleCount, count - visibleCount - (visibleIndex - index)) else 0f

        drawTextInternal(String(text), visibleIndex, visibleIndex + visibleCount, x, y, textWidth, invisiblePrefixWidth, invisiblePostfixWidth, paint)
    }

    private fun drawTextInternal(text: CharSequence, start: Int, end: Int, x: Float, y: Float, paint: Paint) {
        val visibleStart: Int
        val visibleEnd: Int

        if (paint.isUnderlineText || paint.isStrikeThruText) {
            visibleStart = start
            visibleEnd = end
        } else {
            visibleStart = text.getFirstNonWhitespaceCharIndex(start, end) ?: return
            visibleEnd = text.getLastNonWhitespaceCharIndex(start, end) ?: return
        }

        val textWidth = paint.measureText(text, start, end)
        val invisiblePrefixWidth = if (visibleStart != start) paint.measureText(text, start, visibleStart) else 0f
        val invisiblePostfixWidth = if (visibleEnd != end) paint.measureText(text, visibleEnd, end) else 0f

        drawTextInternal(text, visibleStart, visibleEnd, x, y, textWidth, invisiblePrefixWidth, invisiblePostfixWidth, paint)
    }

    private fun drawTextInternal(text: CharSequence, start: Int, end: Int, x: Float, y: Float, textWidth: Float, invisiblePrefixWidth: Float, invisiblePostfixWidth: Float, paint: Paint) {
        if (invisiblePrefixWidth != 0f || invisiblePostfixWidth != 0f) {
            val visibleTextWidth = textWidth - (invisiblePrefixWidth + invisiblePostfixWidth)

            val visibleX = when (paint.textAlign) {
                Paint.Align.LEFT, null ->
                    x + invisiblePrefixWidth
                Paint.Align.CENTER ->
                    x + (invisiblePrefixWidth - invisiblePostfixWidth) / 2f
                Paint.Align.RIGHT ->
                    x - invisiblePostfixWidth
            }

            drawTextInternal(text, start, end, x, y, visibleX, visibleTextWidth, paint, isTextSkeletonsAllowed)
        } else
            drawTextInternal(text, start, end, x, y, x, textWidth, paint, isTextSkeletonsAllowed)
    }

    private fun drawTextInternal(text: CharSequence, start: Int, end: Int, x: Float, y: Float, visibleX: Float, visibleWidth: Float, paint: Paint, isTextSkeletonsAllowed: Boolean) {
        val color = paint.getDrawColor()

        if (Color.alpha(color) == Color.TRANSPARENT)
            return

        val top = translationY + (y + paint.ascent()) * scaleY
        val bottom = translationY + (y + paint.descent()) * scaleY

        if (bottom < clipRect.top || top > clipRect.bottom)
            return

        val scaledTextWidth = visibleWidth * scaleX

        val left: Float
        val right: Float

        when (paint.textAlign) {
            Paint.Align.LEFT, null -> {
                left = translationX + visibleX
                right = left + scaledTextWidth
            }
            Paint.Align.CENTER -> {
                left = translationX + visibleX - scaledTextWidth / 2
                right = left + scaledTextWidth
            }
            Paint.Align.RIGHT -> {
                left = translationX + visibleX - scaledTextWidth
                right = left + scaledTextWidth
            }
        }

        tempRect.set(left, top, right, bottom)

        if (!RectF.intersects(tempRect, clipRect))
            return

        val skeletons = offscreenLayers.lastOrNull()?.skeletons ?: skeletons
        val clipRect = if (!tempRect.isInside(clipRect)) clipRect.toRect() else null

        if (isTextSkeletonsAllowed) {
            val font = paint.typeface?.toFont() ?: Typeface.SANS_SERIF.toFont()

            if (paint.isUnderlineText) {
                val rect = Rect()
                rect.left = tempRect.left.toInt()
                rect.right = tempRect.right.toInt()
                rect.top = (translationY + (y + paint.underlinePositionCompat) * scaleY).toInt()
                rect.bottom = rect.top + paint.underlineThicknessCompat.toInt()

                skeletons += Skeleton.Color(
                    rect = rect,
                    clipRect = clipRect,
                    type = Skeleton.Color.Type.GENERAL,
                    colors = Colors(color),
                    radii = null,
                    flags = null,
                    isOpaque = false
                )
            }

            val wordSpacing = paint.wordSpacingCompat

            if (wordSpacing != 0f) {
                val spaceWidth = paint.measureText(" ")
                val lineRect = RectF(tempRect)

                var lastWordEndLeft = left
                var lastWordStart = -1

                for (i in start until end)
                    if (text[i].isWhitespace()) {
                        if (lastWordStart != -1) {
                            val wordWidth = paint.measureText(text, lastWordStart, i)

                            lineRect.left = lastWordEndLeft
                            lineRect.right = lastWordEndLeft + wordWidth

                            val rect = lineRect.toRect()

                            if (clipRect != null && !Rect.intersects(rect, clipRect)) {
                                lastWordStart = -1
                                break
                            }

                            skeletons += Skeleton.Text(
                                rect = rect,
                                clipRect = if (clipRect != null && !rect.isInside(clipRect)) clipRect else null,
                                text = text.substring(lastWordStart, i),
                                color = color,
                                size = paint.textSize,
                                letterSpacing = paint.letterSpacingCompat,
                                font = font
                            )

                            lastWordEndLeft += wordWidth
                        }

                        lastWordEndLeft += spaceWidth
                        lastWordStart = -1
                    } else if (lastWordStart == -1)
                        lastWordStart = i

                if (lastWordStart != -1) {
                    val wordWidth = paint.measureText(text, lastWordStart, end)

                    lineRect.left = lastWordEndLeft
                    lineRect.right = lastWordEndLeft + wordWidth

                    val rect = lineRect.toRect()

                    if (clipRect == null || Rect.intersects(rect, clipRect))
                        skeletons += Skeleton.Text(
                            rect = rect,
                            clipRect = if (clipRect != null && !rect.isInside(clipRect)) clipRect else null,
                            text = text.substring(lastWordStart, end),
                            color = color,
                            size = paint.textSize,
                            letterSpacing = paint.letterSpacingCompat,
                            font = font
                        )
                }
            } else
                skeletons += Skeleton.Text(
                    rect = tempRect.toRect(),
                    clipRect = clipRect,
                    text = text.substring(start, end),
                    color = color,
                    size = paint.textSize,
                    letterSpacing = paint.letterSpacingCompat,
                    font = font
                )

            if (paint.isStrikeThruText) {
                val rect = Rect()
                rect.left = tempRect.left.toInt()
                rect.right = tempRect.right.toInt()
                rect.top = (translationY + (y + paint.strikeThruPositionCompat) * scaleY).toInt()
                rect.bottom = rect.top + paint.strikeThruThicknessCompat.toInt()

                skeletons += Skeleton.Color(
                    rect = rect,
                    clipRect = clipRect,
                    type = Skeleton.Color.Type.GENERAL,
                    colors = Colors(color),
                    radii = null,
                    flags = null,
                    isOpaque = false
                )
            }
        } else
            skeletons += Skeleton.Color(
                rect = tempRect.toRect(),
                clipRect = clipRect,
                type = Skeleton.Color.Type.TEXT,
                colors = Colors(color),
                radii = null,
                flags = null,
                isOpaque = false
            )
    }

    private fun drawBitmapInternal(bitmap: Bitmap, matrix: Matrix, paint: Paint?) {
        val width = bitmap.width
        val height = bitmap.height

        var dLeft = 0f
        var dTop = 0f
        var dRight = width.toFloat()
        var dBottom = height.toFloat()

        if (!matrix.isIdentity) {
            tempRect.set(dLeft, dTop, dRight, dBottom)
            matrix.mapRect(tempRect)

            dLeft = tempRect.left
            dTop = tempRect.top
            dRight = tempRect.right
            dBottom = tempRect.bottom
        }

        drawBitmapInternal(bitmap, 0, 0, width, height, dLeft, dTop, dRight, dBottom, paint)
    }

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    private fun drawRectInternal(left: Float, top: Float, right: Float, bottom: Float, radii: Radii?, paint: Paint, isFullyCovered: Boolean) {
        val color = Colors(paint.getDrawColor()) // TODO Multicolor
        val flags = paint.getShadowFlag()?.let { Skeleton.Color.Flags(it) }

        when (paint.style) {
            Paint.Style.FILL, Paint.Style.FILL_AND_STROKE -> {
                drawRectInternal(left, top, right, bottom, radii, color, flags, isFullyCovered)
            }
            Paint.Style.STROKE -> {
                if (radii != null && radii.hasRadius()) {
                    // Do not draw skeleton when it is ring
                    // drawRectInternal(left, top, right, bottom, radii, color, flags, isFullyCovered)
                } else {
                    val strokeWidthHalf = paint.strokeWidth / 2f

                    drawRectInternal(left - strokeWidthHalf, top - strokeWidthHalf, right + strokeWidthHalf, top + strokeWidthHalf, null, color, flags, isFullyCovered)
                    drawRectInternal(right - strokeWidthHalf, top + strokeWidthHalf, right + strokeWidthHalf, bottom + strokeWidthHalf, null, color, flags, isFullyCovered)
                    drawRectInternal(left - strokeWidthHalf, bottom - strokeWidthHalf, right - strokeWidthHalf, bottom + strokeWidthHalf, null, color, flags, isFullyCovered)
                    drawRectInternal(left - strokeWidthHalf, top + strokeWidthHalf, left + strokeWidthHalf, bottom - strokeWidthHalf, null, color, flags, isFullyCovered)
                }
            }
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun drawRectInternal(left: Float, top: Float, right: Float, bottom: Float, radii: Radii?, colors: Colors, flags: Skeleton.Color.Flags?, isFullyCovered: Boolean) {
        if (!colors.isClearlyVisible())
            return

        val width = right - left
        val height = bottom - top

        if (!isTextSkeletonsAllowed && (width < RECT_SIZE_THRESHOLD || height < RECT_SIZE_THRESHOLD))
            return

        var left = left * scaleX
        var top = top * scaleY
        var right = right * scaleX
        var bottom = bottom * scaleY

        if (flipHorizontal) {
            val pivotX = clipRect.width()
            left = pivotX - left
            right = pivotX - right

            if (left > right) {
                val temp = left
                left = right
                right = temp
            }
        }

        if (flipVertical) {
            val pivotY = clipRect.height()
            top = pivotY - top
            bottom = pivotY - bottom

            if (top > bottom) {
                val temp = top
                top = bottom
                bottom = temp
            }
        }

        left += translationX
        top += translationY
        right += translationX
        bottom += translationY

        tempRect.set(left, top, right, bottom)

        if (!RectF.intersects(tempRect, clipRect))
            return

        val skeletonRadii = if (clipRadii.hasRadius()) {
            if (radii == null)
                clipRadii.toSkeletonRadii()
            else
                Skeleton.Color.Radii(
                    topLeft = min(clipRadii.topLeft, radii.topLeft),
                    topRight = min(clipRadii.topRight, radii.topRight),
                    bottomRight = min(clipRadii.bottomRight, radii.bottomRight),
                    bottomLeft = min(clipRadii.bottomLeft, radii.bottomLeft)
                )
        } else
            radii?.toSkeletonRadii()

        val skeletonClipRect = if (tempRect.left < clipRect.left || tempRect.right > clipRect.right || tempRect.top < clipRect.top || tempRect.bottom > clipRect.bottom)
            clipRect.toRect()
        else
            null

        actualSkeletons += Skeleton.Color(
            type = Skeleton.Color.Type.GENERAL,
            colors = colors,
            radii = skeletonRadii,
            rect = tempRect.toRect(),
            clipRect = skeletonClipRect,
            flags = flags,
            isOpaque = isFullyCovered && colors.isOpaque()
        )
    }

    private fun drawBitmapInternal(bitmap: Bitmap, src: Rect?, dLeft: Float, dTop: Float, dRight: Float, dBottom: Float, paint: Paint?) {
        val sLeft: Int
        val sTop: Int
        val sRight: Int
        val sBottom: Int

        if (src != null) {
            sLeft = src.left
            sTop = src.top
            sRight = src.right
            sBottom = src.bottom
        } else {
            sLeft = 0
            sTop = 0
            sRight = bitmap.width
            sBottom = bitmap.height
        }

        drawBitmapInternal(bitmap, sLeft, sTop, sRight, sBottom, dLeft, dTop, dRight, dBottom, paint)
    }

    private fun drawBitmapInternal(bitmap: Bitmap, sLeft: Int, sTop: Int, sRight: Int, sBottom: Int, dLeft: Float, dTop: Float, dRight: Float, dBottom: Float, paint: Paint?) {
        val colors = BitmapColorsEstimator.estimate(bitmap, sLeft, sTop, sRight, sBottom, true, paint, false)
        val flags = paint?.getShadowFlag()?.let { Skeleton.Color.Flags(it) }
        drawRectInternal(dLeft, dTop, dRight, dBottom, null, colors, flags, false)
    }

    private fun drawArcInternal(left: Float, top: Float, right: Float, bottom: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean, paint: Paint) {
        if (sweepAngle == 0f)
            return

        var oLeft = left
        var oTop = top
        var oRight = right
        var oBottom = bottom

        if (sweepAngle < 360f) {
            val horizontalRadius = (right - left) / 2f
            val verticalRadius = (bottom - top) / 2f

            val startAngleRes = startAngle % 360f
            val endAngleRes = startAngle + min(sweepAngle, 360f)

            val a = min(startAngleRes, endAngleRes)
            val b = max(startAngleRes, endAngleRes)

            val mod: (Float, Float) -> Float = { n, m -> ((n % m) + m) % m }

            val isCrossing0 = mod(a, 360f) >= mod(b, 360f)
            val isCrossing90 = mod(a - 90f, 360f) >= mod(b - 90f, 360f)
            val isCrossing180 = mod(a - 180f, 360f) >= mod(b - 180f, 360f)
            val isCrossing270 = mod(a - 270f, 360f) >= mod(b - 270f, 360f)

            val aRad = a * PI.toFloat() / 180f
            val bRad = b * PI.toFloat() / 180f

            val ax = horizontalRadius * cos(aRad)
            val ay = verticalRadius * sin(aRad)
            val bx = horizontalRadius * cos(bRad)
            val by = verticalRadius * sin(bRad)

            val cx = left + horizontalRadius
            val cy = top + verticalRadius

            oLeft = if (isCrossing180) left else cx + min(ax, bx)
            oTop = if (isCrossing270) top else cy + min(ay, by)
            oRight = if (isCrossing0) right else cx + max(ax, bx)
            oBottom = if (isCrossing90) bottom else cy + max(ay, by)

            if (useCenter) {
                if (cx < oLeft)
                    oLeft = cx

                if (cy < oTop)
                    oTop = cy

                if (cx > oRight)
                    oRight = cx

                if (cy > oBottom)
                    oBottom = cy
            }
        }

        drawRectInternal(oLeft, oTop, oRight, oBottom, null, paint, false)
    }

    private fun drawPathInternal(path: Path, paint: Paint) {
        val color = Colors(paint.getDrawColor())
        val flags = paint.getShadowFlag()?.let { Skeleton.Color.Flags(it) }
        path.computeBounds(tempRect, false)
        drawRectInternal(tempRect.left, tempRect.top, tempRect.right, tempRect.bottom, null, color, flags, false)
    }

    private fun drawColorInternal(color: Int, mode: PorterDuff.Mode) {
        tempPaint.xfermode = mode.toPorterDuffXfermode()
        tempPaint.color = color

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            tempPaint.blendMode = null

        drawPaintInternal(tempPaint)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun drawColorInternal(color: Int, mode: BlendMode) {
        tempPaint.blendMode = mode
        tempPaint.xfermode = null
        tempPaint.color = color

        drawPaintInternal(tempPaint)
    }

    private fun drawPaintInternal(paint: Paint) {
        val left = clipRect.left - translationX
        val top = clipRect.top - translationY
        val right = clipRect.right - translationX
        val bottom = clipRect.bottom - translationY

        drawRectInternal(left, top, right, bottom, null, paint, true)
    }

    private fun drawLineInternal(startX: Float, startY: Float, stopX: Float, stopY: Float, paint: Paint) {
        val sizeHalf = paint.strokeWidth / 2f

        val left: Float
        val top: Float
        val right: Float
        val bottom: Float

        when {
            startX == stopX -> { // vertical
                left = startX - sizeHalf
                top = startY
                right = stopX + sizeHalf
                bottom = stopY
            }
            startY == stopY -> { // horizontal
                left = startX
                top = startY - sizeHalf
                right = stopX
                bottom = stopY + sizeHalf
            }
            else -> return // Inclined lines are not supported
        }

        drawRectInternal(left, top, right, bottom, null, paint, true)
    }

    private fun drawLinesInternal(pts: FloatArray, offset: Int, count: Int, paint: Paint) {
        for (i in offset until offset + count step 4) {
            val startX = pts[i + 0]
            val startY = pts[i + 1]
            val stopX = pts[i + 2]
            val stopY = pts[i + 3]

            drawLineInternal(startX, startY, stopX, stopY, paint)
        }
    }

    private fun drawPatchInternal(patch: NinePatch, left: Float, top: Float, right: Float, bottom: Float, paint: Paint?) {
        val bitmap = runOnAndroidAtLeast(Build.VERSION_CODES.KITKAT) { patch.bitmap } ?: runCatching { patch.get<Bitmap>("mBitmap") }.getOrNull() ?: return
        val colors = BitmapColorsEstimator.estimate(bitmap, approximation = true, paint = paint, allowMultipleColors = false)
        val flags = paint?.getShadowFlag()?.let { Skeleton.Color.Flags(it) }
        drawRectInternal(left, top, right, bottom, null, colors, flags, false)
    }

    private inline fun runIfSpaceAvailable(crossinline block: () -> Unit) {
        if (skeletons.size < SKELETON_POOL_LIMIT)
            block()
    }

    private data class Layer(
        val saveCount: Int,
        val bounds: RectF,
        val colorFilter: ColorFilter?,
        val xfermode: Xfermode?,
        val alpha: Int?
    ) {

        val skeletons = ArrayList<Skeleton>()
    }

    private data class State(
        val flipHorizontal: Boolean,
        val flipVertical: Boolean,
        val translationX: Float,
        val translationY: Float,
        val scaleX: Float,
        val scaleY: Float,
        val clipRect: RectF,
        val clipRadii: Radii
    )

    private data class Size(
        val width: Int,
        val height: Int
    )

    private data class Line(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float
    )

    private data class Radii(
        var topLeft: Int,
        var topRight: Int,
        var bottomRight: Int,
        var bottomLeft: Int
    ) {

        constructor(radii: Radii) : this(radii.topLeft, radii.topRight, radii.bottomRight, radii.bottomLeft)

        constructor(radius: Int) : this(radius, radius, radius, radius)

        fun toSkeletonRadii(): Skeleton.Color.Radii {
            return Skeleton.Color.Radii(topLeft, topRight, bottomRight, bottomLeft)
        }

        fun set(radii: Radii): Radii {
            topLeft = radii.topLeft
            topRight = radii.topRight
            bottomRight = radii.bottomRight
            bottomLeft = radii.bottomLeft

            return this
        }

        fun set(radius: Int): Radii {
            topLeft = radius
            topRight = radius
            bottomRight = radius
            bottomLeft = radius

            return this
        }

        fun hasRadius(): Boolean {
            return topLeft != 0 || topRight != 0 || bottomLeft != 0 || bottomRight != 0
        }
    }

    private companion object {

        const val TAG = "SkeletonCanvas"

        const val RECT_SIZE_THRESHOLD = 3

        const val SKELETON_POOL_LIMIT = 1000

        val INFINITE_RECT = RectF(-Float.MAX_VALUE, -Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
    }
}
