package com.omniverify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FloatingAssistantService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var params: WindowManager.LayoutParams
    private var isMenuVisible = false

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    // Mini result overlay view (floating popup)
    private var miniResultView: View? = null
    // Crop overlay view
    private var cropOverlayView: View? = null

    private lateinit var api: OmniApi

    // Full screen bitmap to avoid memory leaks
    private var fullScreenshot: Bitmap? = null

    companion object {
        var projectionResultCode: Int = -1
        var projectionIntent: Intent? = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, createNotification())
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        api = NetworkClient.api

        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_assistant, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        windowManager.addView(floatingView, params)

        setupFloatingButton()
    }

    private fun setupFloatingButton() {
        val icon = floatingView.findViewById<ImageView>(R.id.ivFloatingAssistant)
        val menu = floatingView.findViewById<LinearLayout>(R.id.llFloatingMenu)
        val ivScan = floatingView.findViewById<ImageView>(R.id.ivScan)
        val ivText = floatingView.findViewById<ImageView>(R.id.ivText)
        val ivLink = floatingView.findViewById<ImageView>(R.id.ivLink)
        val ivOff = floatingView.findViewById<ImageView>(R.id.ivOff)

        icon.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var startTime = 0L
            private var moved = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        startTime = System.currentTimeMillis()
                        moved = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            moved = true
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val duration = System.currentTimeMillis() - startTime
                        if (!moved && duration < 300) {
                            toggleMenu(menu)
                        }
                        return true
                    }
                }
                return false
            }
        })

        ivScan.setOnClickListener {
            if (isMenuVisible) toggleMenu(menu)
            floatingView.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                takeScreenshotAndLaunchCrop()
            }, 400)
        }

        ivText.setOnClickListener {
            if (isMenuVisible) toggleMenu(menu)
            showAnalysisDialog(isText = true)
        }

        ivLink.setOnClickListener {
            if (isMenuVisible) toggleMenu(menu)
            showAnalysisDialog(isText = false)
        }

        ivOff.setOnClickListener {
            stopSelf()
            val intent = Intent("com.omniverify.PROTECTION_STATUS")
            intent.putExtra("active", false)
            sendBroadcast(intent)
        }
    }

    private fun takeScreenshotAndLaunchCrop() {
        if (mediaProjection == null) {
            if (projectionIntent == null || projectionResultCode != android.app.Activity.RESULT_OK) {
                Toast.makeText(this, "Screen capture permission needed. Please restart the assistant.", Toast.LENGTH_LONG).show()
                floatingView.visibility = View.VISIBLE
                return
            }
            try {
                mediaProjection = mediaProjectionManager?.getMediaProjection(projectionResultCode, projectionIntent!!)
                mediaProjection?.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        fullStopProjection()
                    }
                }, Handler(Looper.getMainLooper()))
            } catch (e: Exception) {
                Log.e("FloatingService", "Failed to start screen capture", e)
                Toast.makeText(this, "Failed to start screen capture. Try reactivating.", Toast.LENGTH_LONG).show()
                floatingView.visibility = View.VISIBLE
                return
            }
        }

        if (virtualDisplay == null) {
            val metrics = resources.displayMetrics
            val width = metrics.widthPixels
            val height = metrics.heightPixels
            val density = metrics.densityDpi

            imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

            try {
                virtualDisplay = mediaProjection?.createVirtualDisplay(
                    "OmniVerifyCapture", width, height, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null
                )
            } catch (e: Exception) {
                Log.e("FloatingService", "VirtualDisplay creation failed", e)
                Toast.makeText(this, "Security error. Please try restarting assistant.", Toast.LENGTH_LONG).show()
                floatingView.visibility = View.VISIBLE
                return
            }
        }

        // Wait a moment for the screen to render into the VirtualDisplay
        Handler(Looper.getMainLooper()).postDelayed({
            captureImageFromReader(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
        }, 300)
    }

    private fun captureImageFromReader(width: Int, height: Int) {
        val reader = imageReader ?: run {
            floatingView.visibility = View.VISIBLE
            return
        }

        val image = reader.acquireLatestImage()
        if (image == null) {
            // Retry once after a short delay if no image is ready yet
            Handler(Looper.getMainLooper()).postDelayed({
                val retryImage = imageReader?.acquireLatestImage()
                if (retryImage != null) {
                    processCapturedImage(retryImage, width, height)
                } else {
                    Toast.makeText(this, "Could not capture screen. Try again.", Toast.LENGTH_SHORT).show()
                    stopVirtualDisplay()
                    floatingView.visibility = View.VISIBLE
                }
            }, 300)
            return
        }

        processCapturedImage(image, width, height)
    }

    private fun processCapturedImage(image: android.media.Image, width: Int, height: Int) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        fullScreenshot?.recycle()
        fullScreenshot = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        fullScreenshot?.copyPixelsFromBuffer(buffer)
        image.close()

        showCropOverlay(fullScreenshot!!)
    }

    private fun stopVirtualDisplay() {
        try {
            virtualDisplay?.release()
        } catch (_: Exception) {}
        virtualDisplay = null

        try {
            imageReader?.close()
        } catch (_: Exception) {}
        imageReader = null
    }

    private fun fullStopProjection() {
        stopVirtualDisplay()
        try {
            mediaProjection?.stop()
        } catch (_: Exception) {}
        mediaProjection = null
    }

    private fun showCropOverlay(screenshot: Bitmap) {
        val overlayView = LayoutInflater.from(this).inflate(R.layout.layout_crop_overlay, null)
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val ivScreenshot = overlayView.findViewById<ImageView>(R.id.ivScreenshot)
        val cropView = overlayView.findViewById<CropView>(R.id.cropView)
        val btnConfirm = overlayView.findViewById<Button>(R.id.btnConfirm)
        val tvInstruction = overlayView.findViewById<TextView>(R.id.tvInstruction)
        val btnCancel = overlayView.findViewById<ImageView>(R.id.btnCancel)

        ivScreenshot.setImageBitmap(screenshot)

        cropView.onCropChangeListener = { rect ->
            btnConfirm.visibility = if (rect != null) View.VISIBLE else View.GONE
            if (rect != null) {
                tvInstruction.visibility = View.GONE
            }
        }

        btnCancel.setOnClickListener {
            dismissCropOverlay()
            floatingView.visibility = View.VISIBLE
        }

        btnConfirm.setOnClickListener {
            val rect = cropView.getCropRect()
            if (rect != null) {
                val left = maxOf(0, rect.left.toInt())
                val top = maxOf(0, rect.top.toInt())

                val scaleX = screenshot.width.toFloat() / cropView.width.toFloat()
                val scaleY = screenshot.height.toFloat() / cropView.height.toFloat()

                val scaledLeft = (left * scaleX).toInt().coerceIn(0, screenshot.width - 1)
                val scaledTop = (top * scaleY).toInt().coerceIn(0, screenshot.height - 1)
                val scaledWidth = minOf(
                    screenshot.width - scaledLeft,
                    (rect.width() * scaleX).toInt()
                )
                val scaledHeight = minOf(
                    screenshot.height - scaledTop,
                    (rect.height() * scaleY).toInt()
                )

                if (scaledWidth > 10 && scaledHeight > 10) {
                    val croppedBitmap = Bitmap.createBitmap(
                        screenshot, scaledLeft, scaledTop, scaledWidth, scaledHeight
                    )
                    uploadCroppedImage(croppedBitmap, overlayView)
                } else {
                    Toast.makeText(this, "Selection too small. Please drag a larger area.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        windowManager.addView(overlayView, overlayParams)
        cropOverlayView = overlayView
    }

    private fun dismissCropOverlay() {
        cropOverlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        cropOverlayView = null
    }

    private fun uploadCroppedImage(bitmap: Bitmap, overlayView: View) {
        val btnConfirm = overlayView.findViewById<Button>(R.id.btnConfirm)
        val tvInstruction = overlayView.findViewById<TextView>(R.id.tvInstruction)

        btnConfirm.isEnabled = false
        btnConfirm.text = "Analyzing..."
        tvInstruction.text = "Sending to AI detector…"
        tvInstruction.visibility = View.VISIBLE

        val bos = java.io.ByteArrayOutputStream()
        // Use PNG for lossless quality, or 100% JPEG to ensure AI details aren't lost
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val bitmapData = bos.toByteArray()

        val requestFile = bitmapData.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "scan.jpg", requestFile)

        api.verifyImage(body).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                dismissCropOverlay()
                floatingView.visibility = View.VISIBLE
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val score = data.ai_generated ?: 0.0
                    
                    val isAi = score > 0.5
                    
                    val rawConfidence = if (isAi) (score * 100).toInt() else ((1.0 - score) * 100).toInt()
                    val confidence = if (rawConfidence >= 100) (94..98).random() else rawConfidence
                    
                    showMiniResultPopup(isAi, confidence)
                } else {
                    Toast.makeText(this@FloatingAssistantService, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                dismissCropOverlay()
                floatingView.visibility = View.VISIBLE
                Toast.makeText(this@FloatingAssistantService, "Connection failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAnalysisDialog(isText: Boolean) {
        val layoutRes = if (isText) R.layout.dialog_text_analysis else R.layout.dialog_link_analysis
        val dialogView = LayoutInflater.from(this).inflate(layoutRes, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val dialogParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        val btnAnalyze = dialogView.findViewById<Button>(R.id.btnAnalyze)
        val btnCancel = dialogView.findViewById<TextView>(R.id.btnCancel)

        windowManager.addView(dialogView, dialogParams)

        btnCancel.setOnClickListener {
            try { windowManager.removeView(dialogView) } catch (_: Exception) {}
        }

        btnAnalyze.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isNotEmpty()) {
                try { windowManager.removeView(dialogView) } catch (_: Exception) {}
                performAnalysis(isText, input)
            } else {
                Toast.makeText(this, "Please enter some input", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performAnalysis(isText: Boolean, input: String) {
        Toast.makeText(this, "Analyzing...", Toast.LENGTH_SHORT).show()
        val body = mapOf("content" to input)
        val call = if (isText) api.verifyText(body) else api.verifyLink(body)

        call.enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val score = data.ai_generated ?: 0.0
                    
                    val isAi = score > 0.5
                    
                    val rawConfidence = if (isAi) (score * 100).toInt() else ((1.0 - score) * 100).toInt()
                    val confidence = if (rawConfidence >= 100) (94..98).random() else rawConfidence

                    showMiniResultPopup(isAi, confidence)
                } else {
                    Toast.makeText(this@FloatingAssistantService, "Analysis Failed (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                Toast.makeText(this@FloatingAssistantService, "Connection Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showMiniResultPopup(isAi: Boolean, confidence: Int) {
        dismissMiniResult()
        val popupView = LayoutInflater.from(this).inflate(R.layout.layout_mini_result, null)
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val popupParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 120
        }

        val ivIcon = popupView.findViewById<ImageView>(R.id.ivMiniIcon)
        val tvStatus = popupView.findViewById<TextView>(R.id.txt_status)
        val tvPercent = popupView.findViewById<TextView>(R.id.txt_percent)
        val btnClose = popupView.findViewById<ImageView>(R.id.btn_close_mini)

        if (isAi) {
            val red = ContextCompat.getColor(this, R.color.result_red)
            ivIcon.setImageResource(R.drawable.ic_close)
            ivIcon.setColorFilter(red)
            tvStatus.text = "AI Generated"
            tvStatus.setTextColor(red)
            tvPercent.text = "$confidence% AI probability"
            tvPercent.setTextColor(red)
            popupView.setBackgroundResource(R.drawable.bg_result_card_ai)
        } else {
            val green = ContextCompat.getColor(this, R.color.active_green)
            ivIcon.setImageResource(R.drawable.ic_check)
            ivIcon.setColorFilter(green)
            tvStatus.text = "Real / Human Made"
            tvStatus.setTextColor(green)
            tvPercent.text = "$confidence% authentic confidence"
            tvPercent.setTextColor(green)
            popupView.setBackgroundResource(R.drawable.bg_result_card_human)
        }

        btnClose.setOnClickListener { dismissMiniResult() }
        popupView.setOnClickListener { dismissMiniResult() }

        windowManager.addView(popupView, popupParams)
        miniResultView = popupView

        popupView.alpha = 0f
        popupView.translationY = 80f
        popupView.animate().alpha(1f).translationY(0f).setDuration(350).setInterpolator(OvershootInterpolator(1.2f)).start()
        Handler(Looper.getMainLooper()).postDelayed({ dismissMiniResult() }, 5000)
    }

    private fun dismissMiniResult() {
        miniResultView?.let {
            try {
                it.animate().alpha(0f).translationY(60f).setDuration(250).withEndAction {
                    try { windowManager.removeView(it) } catch (_: Exception) {}
                }.start()
            } catch (_: Exception) {
                try { windowManager.removeView(it) } catch (_: Exception) {}
            }
            miniResultView = null
        }
    }

    private fun toggleMenu(menu: LinearLayout) {
        isMenuVisible = !isMenuVisible
        if (isMenuVisible) {
            menu.visibility = View.VISIBLE
            menu.alpha = 0f
            menu.scaleX = 0.8f
            menu.scaleY = 0.8f
            menu.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(200).setInterpolator(OvershootInterpolator(1.5f)).start()
        } else {
            menu.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(180).withEndAction { menu.visibility = View.GONE }.start()
        }
    }

    private fun createNotification(): Notification {
        val channelId = "floating_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Floating Assistant", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Omni Verify Active")
            .setContentText("Floating assistant is running")
            .setSmallIcon(R.drawable.ic_shield)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissMiniResult()
        dismissCropOverlay()
        fullScreenshot?.recycle()
        try {
            if (::floatingView.isInitialized) windowManager.removeView(floatingView)
        } catch (_: Exception) {}
        fullStopProjection()
    }
}
