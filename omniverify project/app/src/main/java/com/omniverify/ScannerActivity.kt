package com.omniverify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.omniverify.databinding.ActivityScannerBaseBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBaseBinding
    private lateinit var scanType: String
    private var selectedImageFile: File? = null
    private var targetBitmap: Bitmap? = null
    private var photoUri: Uri? = null

    private val api = NetworkClient.api

    private val pickImage = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { startCropping(it) }
    }

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            photoUri?.let { startCropping(it) }
        } else {
            Toast.makeText(this, "Photo capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityScannerBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Restore state if activity was recreated (e.g. after camera)
        savedInstanceState?.let {
            scanType = it.getString("SCAN_TYPE", "TEXT")
            photoUri = it.getParcelable("PHOTO_URI")
        } ?: run {
            scanType = intent.getStringExtra("SCAN_TYPE") ?: "TEXT"
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupUI()
        setupListeners()
        setupBottomNavigation()

        binding.cropView.onCropChangeListener = { rect ->
            binding.btnConfirmCrop.visibility = if (rect != null && rect.width() > 10 && rect.height() > 10) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SCAN_TYPE", scanType)
        outState.putParcelable("PHOTO_URI", photoUri)
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.isItemActiveIndicatorEnabled = false
        // We don't select any item by default as this is a sub-page, 
        // or we can select multimedia if we consider it part of that flow.
        binding.bottomNavigation.selectedItemId = R.id.nav_multimedia 
        
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_multimedia -> {
                    val intent = Intent(this, MultimediaActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupUI() {
        binding.tvScannerTitle.text = when (scanType) {
            "IMAGE" -> "Image Scanner"
            "QR" -> "QR Code Scanner"
            "TEXT" -> "Text Scanner"
            "LINK" -> "Link Scanner"
            else -> "$scanType Scanner"
        }
        when (scanType) {
            "IMAGE", "QR" -> {
                binding.llUpload.visibility = View.VISIBLE
                binding.llCamera.visibility = View.VISIBLE
                binding.etInput.visibility = View.GONE
                binding.tvUploadText.text = if (scanType == "IMAGE") "Upload from Gallery" else "Upload QR Code"
                binding.tvCameraText.text = if (scanType == "IMAGE") "Take Photo" else "Scan QR Code"
            }
            "TEXT", "LINK" -> {
                binding.llUpload.visibility = View.GONE
                binding.llCamera.visibility = View.GONE
                binding.etInput.visibility = View.VISIBLE
                binding.etInput.hint = if (scanType == "TEXT") "Paste text here..." else "Enter URL here..."
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.llUpload.setOnClickListener { 
            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
        }
        binding.llCamera.setOnClickListener { 
            checkPermissionAndLaunchCamera()
        }
        binding.btnAnalyze.setOnClickListener { performAnalysis() }
        
        binding.btnConfirmCrop.setOnClickListener { confirmCrop() }
        binding.btnCancelCrop.setOnClickListener { 
            binding.clCropLayer.visibility = View.GONE
        }
    }

    private fun checkPermissionAndLaunchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        if (storageDir == null) {
            Toast.makeText(this, "Storage not available", Toast.LENGTH_SHORT).show()
            return
        }
        val photoFile = File(storageDir, "photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        photoUri = uri
        takePhoto.launch(uri)
    }

    private fun startCropping(uri: Uri) {
        try {
            // Use BitmapFactory.Options to downscale large images to avoid OOM
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options)
            }
            
            val maxDimension = 2048
            var inSampleSize = 1
            if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while (halfHeight / inSampleSize >= maxDimension && halfWidth / inSampleSize >= maxDimension) {
                    inSampleSize *= 2
                }
            }
            
            options.inJustDecodeBounds = false
            options.inSampleSize = inSampleSize
            
            val bitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            
            if (bitmap != null) {
                targetBitmap = bitmap
                binding.ivCropPreview.setImageBitmap(bitmap)
                binding.clCropLayer.visibility = View.VISIBLE
                // Start with a clean slate for manual drawing
                binding.cropView.clear()
                binding.btnConfirmCrop.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmCrop() {
        val bitmap = targetBitmap ?: return
        val cropRect = binding.cropView.getCropRect() ?: return

        // Calculate actual crop coordinates based on image vs view scale
        val viewWidth = binding.ivCropPreview.width.toFloat()
        val viewHeight = binding.ivCropPreview.height.toFloat()
        val imgWidth = bitmap.width.toFloat()
        val imgHeight = bitmap.height.toFloat()

        val scale: Float
        val dx: Float
        val dy: Float

        if (imgWidth / imgHeight > viewWidth / viewHeight) {
            scale = viewWidth / imgWidth
            dx = 0f
            dy = (viewHeight - imgHeight * scale) / 2f
        } else {
            scale = viewHeight / imgHeight
            dx = (viewWidth - imgWidth * scale) / 2f
            dy = 0f
        }

        val actualLeft = ((cropRect.left - dx) / scale).toInt().coerceIn(0, bitmap.width)
        val actualTop = ((cropRect.top - dy) / scale).toInt().coerceIn(0, bitmap.height)
        val actualRight = ((cropRect.right - dx) / scale).toInt().coerceIn(0, bitmap.width)
        val actualBottom = ((cropRect.bottom - dy) / scale).toInt().coerceIn(0, bitmap.height)

        val width = (actualRight - actualLeft).coerceAtLeast(1)
        val height = (actualBottom - actualTop).coerceAtLeast(1)

        try {
            val croppedBitmap = Bitmap.createBitmap(bitmap, actualLeft, actualTop, width, height)
            binding.clCropLayer.visibility = View.GONE
            
            if (scanType == "QR") {
                processQRLocally(croppedBitmap)
            } else {
                uploadCroppedImage(croppedBitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Crop failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processQRLocally(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val qrContent = barcodes[0].rawValue ?: ""
                    api.verifyQr(mapOf("qr_content" to qrContent)).enqueue(object : Callback<VerifyResponse> {
                        override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                            handleResponse(response)
                        }
                        override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                            Toast.makeText(this@ScannerActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "No QR code detected in the cropped area", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "QR Detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadCroppedImage(bitmap: Bitmap) {
        showLoading(true)
        
        // 1. Downscale only if unusually large. 1600px keeps upload time
        //    reasonable while preserving far more fine detail than the old
        //    1024px cap -- important for spotting hybrid/partial AI edits.
        val maxDimension = 1600
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val width = bitmap.width
            val height = bitmap.height
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

        // 2. Compress to JPEG 95% -- the backend no longer re-encodes this
        //    image, so this is the only compression pass it goes through.
        val stream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        val byteArray = stream.toByteArray()
        
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", "cropped_image.jpg", requestBody)

        api.verifyImage(body).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                showLoading(false)
                handleResponse(response)
            }
            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@ScannerActivity, "Connection error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun performAnalysis() {
        when (scanType) {
            "IMAGE", "QR" -> {
                if (targetBitmap != null) {
                    // Show crop layer again if they want to re-crop
                    binding.clCropLayer.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                }
            }
            "TEXT" -> verifyText(binding.etInput.text.toString())
            "LINK" -> verifyLink(binding.etInput.text.toString())
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnAnalyze.isEnabled = !isLoading
        binding.llUpload.isEnabled = !isLoading
        binding.llCamera.isEnabled = !isLoading
    }

    private fun verifyText(text: String) {
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter text", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        api.verifyText(mapOf("text_content" to text)).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                showLoading(false)
                handleResponse(response)
            }
            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@ScannerActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun verifyLink(url: String) {
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
            return
        }
        showLoading(true)
        api.verifyLink(mapOf("url_content" to url)).enqueue(object : Callback<VerifyResponse> {
            override fun onResponse(call: Call<VerifyResponse>, response: Response<VerifyResponse>) {
                showLoading(false)
                handleResponse(response)
            }
            override fun onFailure(call: Call<VerifyResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@ScannerActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleResponse(response: Response<VerifyResponse>) {
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            
            val aiProb = body.ai_generated ?: 0.0
            var verdict = body.verdict ?: if (aiProb > 0.75) "AI" else if (aiProb >= 0.15) "PARTIAL_AI" else "HUMAN"
            
            // Normalize "CLEAN" from backend to "HUMAN" for UI consistency
            if (verdict == "CLEAN") verdict = "HUMAN"
            
            // Calculate confidence
            val confidenceInt = when (verdict) {
                "DANGEROUS", "DANGEROUS_SCRIPT" -> 99 // High confidence for security threats
                "HUMAN" -> ((1.0 - aiProb) * 100).toInt().coerceIn(0, 100)
                else -> (aiProb * 100).toInt().coerceIn(0, 100)
            }
            
            saveScanToHistory(body.copy(verdict = verdict), confidenceInt)
            
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("verdict", verdict)
                putExtra("confidence", confidenceInt)
                putExtra("summary", body.summary)
                putStringArrayListExtra("markers", ArrayList(body.markers ?: emptyList()))
            }
            startActivity(intent)
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Toast.makeText(this, "Analysis failed: $errorBody", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveScanToHistory(body: VerifyResponse, confidence: Int) {
        val rawContent = when (scanType) {
            "TEXT", "LINK" -> binding.etInput.text.toString()
            "QR" -> "QR Code Scan"
            "IMAGE" -> "Image Scan"
            else -> ""
        }
        
        val scan = ScanHistory(
            scanType = scanType,
            rawContent = rawContent,
            verdict = body.verdict ?: "UNKNOWN",
            confidence = confidence
        )
        
        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(this@ScannerActivity).scanHistoryDao().insert(scan)
        }
    }
}