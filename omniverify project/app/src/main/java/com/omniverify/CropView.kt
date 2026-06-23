package com.omniverify

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var cropRect: RectF? = null
    var onCropChangeListener: ((RectF?) -> Unit)? = null

    private enum class TouchMode { NONE, DRAWING, MOVING, RESIZING }
    private var mode = TouchMode.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // 0-3: Corners (TL, TR, BR, BL), 4-7: Sides (Top, Right, Bottom, Left)
    private var activeHandle = -1 
    private val handleRadius = 45f 

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
    }

    private val handlePaint = Paint().apply {
        color = Color.parseColor("#00FF9D") // Brand Green
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val eraserPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#99000000") // Darker overlay
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    fun clear() {
        cropRect = null
        invalidate()
    }

    fun resetCropRect(w: Int = width, h: Int = height) {
        if (w > 0 && h > 0) {
            val size = Math.min(w, h) * 0.7f
            val left = (w - size) / 2
            val top = (h - size) / 2
            cropRect = RectF(left, top, left + size, top + size)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        cropRect?.let { rect ->
            canvas.drawRect(rect, eraserPaint)
            canvas.drawRect(rect, paint)

            // Draw Handles
            // Corners
            drawHandle(canvas, rect.left, rect.top)
            drawHandle(canvas, rect.right, rect.top)
            drawHandle(canvas, rect.right, rect.bottom)
            drawHandle(canvas, rect.left, rect.bottom)
            
            // Sides
            drawHandle(canvas, rect.centerX(), rect.top)
            drawHandle(canvas, rect.right, rect.centerY())
            drawHandle(canvas, rect.centerX(), rect.bottom)
            drawHandle(canvas, rect.left, rect.centerY())
        }
    }

    private fun drawHandle(canvas: Canvas, x: Float, y: Float) {
        canvas.drawCircle(x, y, 12f, handlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = x
                lastTouchY = y
                
                activeHandle = getClickedHandle(x, y)
                if (activeHandle != -1) {
                    mode = TouchMode.RESIZING
                } else if (cropRect?.contains(x, y) == true) {
                    mode = TouchMode.MOVING
                } else {
                    mode = TouchMode.DRAWING
                    cropRect = RectF(x, y, x, y)
                }
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - lastTouchX
                val dy = y - lastTouchY

                when (mode) {
                    TouchMode.DRAWING -> {
                        cropRect?.let { it.right = x; it.bottom = y }
                    }
                    TouchMode.MOVING -> {
                        cropRect?.offset(dx, dy)
                    }
                    TouchMode.RESIZING -> {
                        resizeRect(dx, dy)
                    }
                    else -> {}
                }
                
                lastTouchX = x
                lastTouchY = y
                normalizeBounds()
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                mode = TouchMode.NONE
                onCropChangeListener?.invoke(getCropRect())
            }
        }
        return true
    }

    private fun getClickedHandle(x: Float, y: Float): Int {
        val rect = cropRect ?: return -1
        // Check Corners
        if (isNear(x, y, rect.left, rect.top)) return 0
        if (isNear(x, y, rect.right, rect.top)) return 1
        if (isNear(x, y, rect.right, rect.bottom)) return 2
        if (isNear(x, y, rect.left, rect.bottom)) return 3
        // Check Sides
        if (isNear(x, y, rect.centerX(), rect.top)) return 4
        if (isNear(x, y, rect.right, rect.centerY())) return 5
        if (isNear(x, y, rect.centerX(), rect.bottom)) return 6
        if (isNear(x, y, rect.left, rect.centerY())) return 7
        return -1
    }

    private fun isNear(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return Math.hypot((x1 - x2).toDouble(), (y1 - y2).toDouble()) < handleRadius
    }

    private fun resizeRect(dx: Float, dy: Float) {
        cropRect?.let {
            when (activeHandle) {
                0 -> { it.left += dx; it.top += dy } // TL
                1 -> { it.right += dx; it.top += dy } // TR
                2 -> { it.right += dx; it.bottom += dy } // BR
                3 -> { it.left += dx; it.bottom += dy } // BL
                4 -> { it.top += dy } // Top Side
                5 -> { it.right += dx } // Right Side
                6 -> { it.bottom += dy } // Bottom Side
                7 -> { it.left += dx } // Left Side
            }
        }
    }

    private fun normalizeBounds() {
        cropRect?.let {
            it.left = it.left.coerceIn(0f, width.toFloat())
            it.right = it.right.coerceIn(0f, width.toFloat())
            it.top = it.top.coerceIn(0f, height.toFloat())
            it.bottom = it.bottom.coerceIn(0f, height.toFloat())
        }
    }

    fun getCropRect(): RectF? {
        val rect = cropRect ?: return null
        return RectF(
            Math.min(rect.left, rect.right),
            Math.min(rect.top, rect.bottom),
            Math.max(rect.left, rect.right),
            Math.max(rect.top, rect.bottom)
        )
    }
}
