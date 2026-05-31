package com.omniverify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private var isServiceActive = false
    private lateinit var btnToggle: FrameLayout
    private lateinit var ivToggleIcon: ImageView
    private lateinit var tvToggleAction: TextView
    private lateinit var statusDot: android.view.View
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvStatusMessage: TextView

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Settings.canDrawOverlays(this)) {
            requestMediaProjection()
        } else {
            Toast.makeText(this, "Permission denied. Overlay permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    private val projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // Store BOTH resultCode and data — needed to correctly create MediaProjection each time
            FloatingAssistantService.projectionResultCode = result.resultCode
            FloatingAssistantService.projectionIntent = result.data
            startFloatingService()
        } else {
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val statusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val active = intent?.getBooleanExtra("active", false) ?: false
            if (!active) {
                updateUI(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnToggle = findViewById(R.id.btnToggle)
        ivToggleIcon = findViewById(R.id.ivToggleIcon)
        tvToggleAction = findViewById(R.id.tvToggleAction)
        statusDot = findViewById(R.id.statusDot)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvStatusMessage = findViewById(R.id.tvStatusMessage)

        btnToggle.setOnClickListener {
            if (isServiceActive) {
                stopFloatingService()
            } else {
                checkPermissionAndStart()
            }
        }

        val filter = IntentFilter("com.omniverify.PROTECTION_STATUS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(statusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(statusReceiver, filter)
        }
    }

    private fun checkPermissionAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            requestMediaProjection()
        }
    }

    private fun requestMediaProjection() {
        val manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projectionLauncher.launch(manager.createScreenCaptureIntent())
    }

    private fun startFloatingService() {
        isServiceActive = true
        startService(Intent(this, FloatingAssistantService::class.java))
        updateUI(true)
    }

    private fun stopFloatingService() {
        isServiceActive = false
        stopService(Intent(this, FloatingAssistantService::class.java))
        updateUI(false)
    }

    private fun updateUI(active: Boolean) {
        isServiceActive = active
        if (active) {
            btnToggle.setBackgroundResource(R.drawable.bg_circle_active)
            ivToggleIcon.setImageResource(R.drawable.ic_power)
            ivToggleIcon.setColorFilter(ContextCompat.getColor(this, R.color.active_green))
            tvToggleAction.text = getString(R.string.deactivate)
            tvToggleAction.setTextColor(ContextCompat.getColor(this, R.color.active_green))

            statusDot.setBackgroundResource(R.drawable.bg_dot_active)
            tvStatusBadge.text = getString(R.string.protection_active)
            tvStatusMessage.text = getString(R.string.assistant_active)
        } else {
            btnToggle.setBackgroundResource(R.drawable.bg_circle_outer)
            ivToggleIcon.setImageResource(R.drawable.ic_shield)
            ivToggleIcon.setColorFilter(ContextCompat.getColor(this, R.color.inactive_blue))
            tvToggleAction.text = getString(R.string.activate)
            tvToggleAction.setTextColor(ContextCompat.getColor(this, R.color.inactive_blue))

            statusDot.setBackgroundResource(R.drawable.bg_dot_inactive)
            tvStatusBadge.text = getString(R.string.protection_inactive)
            tvStatusMessage.text = getString(R.string.tap_to_enable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(statusReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
