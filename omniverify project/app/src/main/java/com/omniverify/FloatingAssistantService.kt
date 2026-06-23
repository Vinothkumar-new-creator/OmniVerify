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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
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

    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { tuckAssistantIntoCorner() }
    private var isTucked = false

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
        setTheme(R.style.Theme_Omniverify)
        super.onCreate()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, createNotification())
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        api = NetworkClient.api
        NetworkClient.warmUp()

        // Use ContextThemeWrapper to resolve theme attributes like ?attr/selectableItemBackground
        val contextWrapper = androidx.appcompat.view.ContextThemeWrapper(this, R.style.Theme_Omniverify)
        floatingView = LayoutInflater.from(contextWrapper).inflate(R.layout.layout_floating_assistant, null)

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
        resetHideTimer()
    }

    private fun resetHideTimer() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 30000)
    }

    private fun tuckAssistantIntoCorner() {
        if (isTucked || isMenuVisible) return
        isTucked = true
        floatingView.animate()
            .alpha(0.5f)
            .translationX(if (params.x > resources.displayMetrics.widthPixels / 2) 50f else -50f)
            .setDuration(500)
            .start()
    }

    private fun untuckAssistantFromCorner() {
        if (!isTucked) return
        isTucked = false
        floatingView.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(300)
            .start()
        resetHideTimer()
    }

    private fun setupFloatingButton() {
        val icon = floatingView.findViewById<ImageView>(R.id.ivFloatingAssistant)
        val menu = floatingView.findViewById<LinearLayout>(R.id.llFloatingMenu)
        val ivScan = floatingView.findViewById<ImageView>(R.id.ivScan)
        val ivText = floatingView.findViewById<ImageView>(R.id.ivText)
        val ivLink = floatingView.findViewById<ImageView>(R.id.ivLink)
        val ivQr = floatingView.findViewById<ImageView>(R.id.ivQr)
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
                        untuckAssistantFromCorner()
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        startTime = System.currentTimeMillis()
                        moved = false
                        resetHideTimer()
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        resetHideTimer()
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
                takeScreenshotAndLaunchCrop(isQr = false)
            }, 400)
        }

        ivQr.setOnClickListener {
            if (isMenuVisible) toggleMenu(menu)
            floatingView.visibility = View.GONE
            Handler(Looper.getMainLooper()).postDelayed({
                takeScreenshotAndLaunchCrop(isQr = true)
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
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }

    private fun takeScreenshotAndLaunchCrop(isQr: Boolean) {
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
            captureImageFromReader(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels, isQr)
        }, 300)
    }

    private fun captureImageFromReader(width: Int, height: Int, isQr: Boolean) {
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
                    processCapturedImage(retryImage, width, height, isQr)
                } else {
                    Toast.makeText(this, "Could not capture screen. Try again.", Toast.LENGTH_SHORT).show()
                    stopVirtualDisplay()
                    floatingView.visibility = View.VISIBLE
                }
            }, 300)
            return
        }

        processCapturedImage(image, width, height, isQr)
    }

    private fun processCapturedImage(image: android.media.Image, width: Int, height: Int, isQr: Boolean) {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * width

        fullScreenshot?.recycle()
        fullScreenshot = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
        fullScreenshot?.copyPixelsFromBuffer(buffer)
        image.close()

        showCropOverlay(fullScreenshot!!, isQr)
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

    private fun showCropOverlay(screenshot: Bitmap, isQr: Boolean) {
        val contextWrapper = androidx.appcompat.view.ContextThemeWrapper(this, R.style.Theme_Omniverify)
        val overlayView = LayoutInflater.from(contextWrapper).inflate(R.layout.layout_crop_overlay, null)
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

        if (isQr) {
            tvInstruction.text = getString(R.string.qr_instruction)
            btnConfirm.text = getString(R.string.scan_qr)
        }

        ivScreenshot.setImageBitmap(screenshot)

        // Ensure clear state for manual drawing
        cropView.clear()
        btnConfirm.visibility = View.GONE

        cropView.onCropChangeListener = { rect ->
            btnConfirm.visibility = if (rect != null && rect.width() > 10 && rect.height() > 10) {
                View.VISIBLE
            } else {
                View.GONE
            }
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
                    if (isQr) {
                        scanAndVerifyQr(croppedBitmap, overlayView)
                    } else {
                        uploadCroppedImage(croppedBitmap, overlayView)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.selection_too_small), Toast.LENGTH_SHORT).show()
                }
            }
        }

        windowManager.addView(overlayView, overlayParams)
        cropOverlayView = overlayView
    }

    private fun scanAndVerifyQr(bitmap: Bitmap, overlayView: View) {
        val btnConfirm = overlayView.findViewById<Button>(R.id.btnConfirm)
        val tvInstruction = overlayView.findViewById<TextView>(R.id.tvInstruction)

        btnConfirm.isEnabled = false
        btnConfirm.text = getString(R.string.scanning)
        tvInstruction.text = getString(R.string.decoding_qr)
        tvInstruction.visibility = View.VISIBLE

        val image = com.google.mlkit.vision.common.InputImage.fromBitmap(bitmap, 0)
        val scanner = com.google.mlkit.vision.barcode.BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val qrData = barcodes[0].rawValue ?: ""
                    if (qrData.isNotEmpty()) {
                        sendQrToBackend(qrData)
                    } else {
                        Toast.makeText(this, getString(R.string.empty_qr_data), Toast.LENGTH_SHORT).show()
                    }
                    dismissCropOverlay()
                    floatingView.visibility = View.VISIBLE
                } else {
                    btnConfirm.isEnabled = true
                    btnConfirm.text = getString(R.string.scan_qr)
                    tvInstruction.text = getString(R.string.no_qr_detected)
                    Toast.makeText(this, getString(R.string.no_qr_found), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                btnConfirm.isEnabled = true
                btnConfirm.text = getString(R.string.scan_qr)
                tvInstruction.text = getString(R.string.scan_failed)
                Toast.makeText(this, getString(R.string.qr_scan_failed_prefix, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendQrToBackend(qrData: String) {
        Toast.makeText(this, getString(R.string.verifying_qr), Toast.LENGTH_SHORT).show()
        val body = mapOf("qr_content" to qrData)
        api.verifyQr(body).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val score = data.ai_generated ?: 0.0
                    var verdict = data.verdict ?: if (score > 0.75) "AI" else if (score >= 0.15) "PARTIAL_AI" else "HUMAN"
                    
                    if (verdict == "CLEAN") verdict = "HUMAN"

                    val confidence = when (verdict) {
                        "DANGEROUS", "DANGEROUS_SCRIPT" -> 99
                        "AI", "PARTIAL_AI" -> (score * 100).toInt()
                        else -> ((1.0 - score) * 100).toInt()
                    }.coerceIn(0, 100)
                    
                    val finalConfidence = if (confidence >= 100) (94..98).random() else confidence
                    showMiniResultPopup(verdict, finalConfidence)
                } else {
                    Toast.makeText(this@FloatingAssistantService, getString(R.string.qr_analysis_failed), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                Toast.makeText(this@FloatingAssistantService, getString(R.string.connection_error_prefix, t.message), Toast.LENGTH_SHORT).show()
            }
        })
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
        btnConfirm.text = getString(R.string.analyzing)
        tvInstruction.text = getString(R.string.sending_to_ai)
        tvInstruction.visibility = View.VISIBLE

        // 1. Downscale only if the crop is unusually large. 1600px keeps payload
        //    size (and upload time) reasonable on slow connections while
        //    preserving far more of the fine detail AI/deepfake detection
        //    relies on than the old 1024px cap did.
        val maxDimension = 1600
        val width = bitmap.width
        val height = bitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val newWidth: Int
            val newHeight: Int
            if (width > height) {
                newWidth = maxDimension
                newHeight = (height * (maxDimension.toFloat() / width)).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (width * (maxDimension.toFloat() / height)).toInt()
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        // 2. Compress to JPEG 95% -- this is now the ONLY compression pass
        //    (the backend no longer re-encodes the image), so we can afford
        //    much higher quality without increasing total upload size versus
        //    the old double-compression pipeline.
        val bos = java.io.ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, bos)
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
                    var verdict = data.verdict ?: if (score > 0.75) "AI" else if (score >= 0.15) "PARTIAL_AI" else "HUMAN"
                    
                    if (verdict == "CLEAN") verdict = "HUMAN"

                    val confidence = when (verdict) {
                        "DANGEROUS", "DANGEROUS_SCRIPT" -> 99
                        "AI", "PARTIAL_AI" -> (score * 100).toInt()
                        else -> ((1.0 - score) * 100).toInt()
                    }.coerceIn(0, 100)
                    
                    val finalConfidence = if (confidence >= 100) (94..98).random() else confidence
                    
                    showMiniResultPopup(verdict, finalConfidence)
                } else {
                    Toast.makeText(this@FloatingAssistantService, getString(R.string.server_error_prefix, response.code()), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                dismissCropOverlay()
                floatingView.visibility = View.VISIBLE
                Toast.makeText(this@FloatingAssistantService, getString(R.string.connection_failed_prefix, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAnalysisDialog(isText: Boolean) {
        // Use ContextThemeWrapper to provide a theme for attribute resolution (e.g. ?attr/selectableItemBackgroundBorderless)
        val contextWrapper = androidx.appcompat.view.ContextThemeWrapper(this, R.style.Theme_Omniverify)
        val layoutRes = if (isText) R.layout.dialog_text_analysis else R.layout.dialog_link_analysis
        val dialogView = LayoutInflater.from(contextWrapper).inflate(layoutRes, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val metrics = resources.displayMetrics
        val density = metrics.density
        
        // Target a generous size initially to prevent clipping
        val targetWidthDp = 320
        val targetHeightDp = if (isText) 400 else 340
        
        // Ensure we don't exceed screen size on smaller devices
        val initialWidth = minOf((targetWidthDp * density).toInt(), (metrics.widthPixels * 0.95f).toInt())
        val initialHeight = minOf((targetHeightDp * density).toInt(), (metrics.heightPixels * 0.85f).toInt())

        val dialogParams = WindowManager.LayoutParams(
            initialWidth,
            initialHeight,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            // Use ADJUST_PAN to prevent the window from expanding to full screen when keyboard opens
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        }

        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        val btnAnalyze = dialogView.findViewById<Button>(R.id.btnAnalyze)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)
        val ivResizeHandle = dialogView.findViewById<ImageView>(R.id.ivResizeHandle)
        val dialogRoot = dialogView.findViewById<View>(R.id.dialog_root)

        windowManager.addView(dialogView, dialogParams)

        // Ensure the input box is cleared and ready for new input
        etInput.text.clear()
        etInput.postDelayed({
            etInput.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(etInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }, 100)

        // Make Dialog Draggable
        dialogRoot.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = dialogParams.x
                        initialY = dialogParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        dialogParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        dialogParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(dialogView, dialogParams)
                        return true
                    }
                }
                return false
            }
        })

        // Make Dialog Resizable
        ivResizeHandle.setOnTouchListener(object : View.OnTouchListener {
            private var initialWidthVal = 0
            private var initialHeightVal = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialWidthVal = dialogParams.width
                        initialHeightVal = dialogParams.height
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        // Resizing Logic: Add finger movement (delta) to initial size
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()
                        
                        // Use a minimum size that guarantees the input box and buttons stay visible
                        // We use a larger minHeight for text (more content) than for links
                        val minWidth = (280 * density).toInt()
                        val minHeight = if (isText) (320 * density).toInt() else (280 * density).toInt()
                        
                        dialogParams.width = maxOf(minWidth, initialWidthVal + deltaX)
                        dialogParams.height = maxOf(minHeight, initialHeightVal + deltaY)
                        windowManager.updateViewLayout(dialogView, dialogParams)
                        return true
                    }
                }
                return false
            }
        })

        btnCancel.setOnClickListener {
            try { windowManager.removeView(dialogView) } catch (_: Exception) {}
        }

        btnAnalyze.setOnClickListener {
            val input = etInput.text.toString().trim()
            if (input.isNotEmpty()) {
                try { windowManager.removeView(dialogView) } catch (_: Exception) {}
                performAnalysis(isText, input)
            } else {
                Toast.makeText(this, getString(R.string.please_enter_input), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performAnalysis(isText: Boolean, input: String) {
        Toast.makeText(this, getString(R.string.analyzing), Toast.LENGTH_SHORT).show()
        val key = if (isText) "text_content" else "url_content"
        val body = mapOf(key to input)
        val call = if (isText) api.verifyText(body) else api.verifyLink(body)

        call.enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    val score = data.ai_generated ?: 0.0
                    var verdict = data.verdict ?: if (score > 0.75) "AI" else if (score >= 0.15) "PARTIAL_AI" else "HUMAN"
                    
                    if (verdict == "CLEAN") verdict = "HUMAN"

                    val confidence = when (verdict) {
                        "DANGEROUS", "DANGEROUS_SCRIPT" -> 99
                        "AI", "PARTIAL_AI" -> (score * 100).toInt()
                        else -> ((1.0 - score) * 100).toInt()
                    }.coerceIn(0, 100)
                    
                    val finalConfidence = if (confidence >= 100) (94..98).random() else confidence

                    showMiniResultPopup(verdict, finalConfidence)
                } else {
                    Toast.makeText(this@FloatingAssistantService, getString(R.string.analysis_failed_prefix, response.code()), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                Toast.makeText(this@FloatingAssistantService, getString(R.string.connection_error_prefix, t.message), Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showMiniResultPopup(verdict: String, confidence: Int) {
        dismissMiniResult()
        val contextWrapper = androidx.appcompat.view.ContextThemeWrapper(this, R.style.Theme_Omniverify)
        val popupView = LayoutInflater.from(contextWrapper).inflate(R.layout.layout_mini_result, null)
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

        when (verdict) {
            "AI", "DANGEROUS", "DANGEROUS_SCRIPT" -> {
                val red = ContextCompat.getColor(this, R.color.result_red)
                ivIcon.setImageResource(R.drawable.ic_close)
                ivIcon.setColorFilter(red)
                tvStatus.text = if (verdict == "AI") getString(R.string.ai_generated) else "Dangerous Content"
                tvStatus.setTextColor(red)
                tvPercent.text = if (verdict == "AI") getString(R.string.ai_probability_suffix, confidence) else "High Risk Detected"
                tvPercent.setTextColor(red)
                popupView.setBackgroundResource(R.drawable.bg_result_card_ai)
            }
            "PARTIAL_AI" -> {
                val yellow = ContextCompat.getColor(this, R.color.result_yellow)
                ivIcon.setImageResource(R.drawable.ic_shield)
                ivIcon.setColorFilter(yellow)
                tvStatus.text = getString(R.string.partial_ai)
                tvStatus.setTextColor(yellow)
                tvPercent.text = getString(R.string.ai_probability_suffix, confidence)
                tvPercent.setTextColor(yellow)
                popupView.setBackgroundResource(R.drawable.bg_result_card_partial)
            }
            else -> {
                val green = ContextCompat.getColor(this, R.color.active_green)
                ivIcon.setImageResource(R.drawable.ic_check)
                ivIcon.setColorFilter(green)
                tvStatus.text = getString(R.string.human_made)
                tvStatus.setTextColor(green)
                tvPercent.text = getString(R.string.authentic_confidence_suffix, confidence)
                tvPercent.setTextColor(green)
                popupView.setBackgroundResource(R.drawable.bg_result_card_human)
            }
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
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
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
