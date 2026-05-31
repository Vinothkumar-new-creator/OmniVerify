package com.omniverify

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var startX = 0f
    private var startY = 0f
    private var currentX = 0f
    private var currentY = 0f
    private var isDrawing = false
    
    var onCropChangeListener: ((RectF?) -> Unit)? = null

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val eraserPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#80000000") // 50% transparent black
    }

    private var cropRect: RectF? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the dimmed overlay
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        cropRect?.let {
            // Clear the area inside the rectangle
            canvas.drawRect(it, eraserPaint)
            // Draw the white dashed border
            canvas.drawRect(it, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDrawing = true
                cropRect = null
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                currentX = event.x
                currentY = event.y
                updateRect()
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                isDrawing = false
                onCropChangeListener?.invoke(cropRect)
            }
        }
        return true
    }

    private fun updateRect() {
        val left = Math.min(startX, currentX)
        val top = Math.min(startY, currentY)
        val right = Math.max(startX, currentX)
        val bottom = Math.max(startY, currentY)
        cropRect = RectF(left, top, right, bottom)
    }

    fun getCropRect(): RectF? = cropRect
    
    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
}
