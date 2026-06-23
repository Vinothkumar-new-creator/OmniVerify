package com.omniverify

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var isServiceActive = false
    private lateinit var btnToggle: FrameLayout
    private lateinit var ivToggleIcon: ImageView
    private lateinit var tvToggleAction: TextView
    private lateinit var statusDot: android.view.View
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvStatusMessage: TextView
    private lateinit var rvHistory: RecyclerView
    private lateinit var historyAdapter: ScanHistoryAdapter
    private lateinit var clHistoryOverlay: android.view.View
    private lateinit var ivMenu: ImageView
    private lateinit var btnCloseHistory: ImageView

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

        // Fire this as early as possible: if the backend is on a host that
        // sleeps when idle (e.g. Render free tier), this gives it a head
        // start so it's already awake by the time the user crops an image.
        NetworkClient.warmUp()

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            bottomNav.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        btnToggle = findViewById(R.id.btnToggle)
        ivToggleIcon = findViewById(R.id.ivToggleIcon)
        tvToggleAction = findViewById(R.id.tvToggleAction)
        statusDot = findViewById(R.id.statusDot)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvStatusMessage = findViewById(R.id.tvStatusMessage)
        rvHistory = findViewById(R.id.rvHistory)
        clHistoryOverlay = findViewById(R.id.clHistoryOverlay)
        ivMenu = findViewById(R.id.ivMenu)
        btnCloseHistory = findViewById(R.id.btnCloseHistory)

        ivMenu.setOnClickListener {
            showHistory(true)
        }

        btnCloseHistory.setOnClickListener {
            showHistory(false)
        }

        setupHistoryRecyclerView()

        btnToggle.setOnClickListener {
            if (isServiceActive) {
                stopFloatingService()
            } else {
                checkPermissionAndStart()
            }
        }

        setupBottomNavigation()

        val filter = IntentFilter("com.omniverify.PROTECTION_STATUS")
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_home
    }

    private fun setupHistoryRecyclerView() {
        historyAdapter = ScanHistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter

        lifecycleScope.launch {
            AppDatabase.getDatabase(this@MainActivity).scanHistoryDao().getAllScans().collectLatest { scans ->
                historyAdapter.submitList(scans)
            }
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.isItemActiveIndicatorEnabled = false
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_multimedia -> {
                    startActivity(Intent(this, MultimediaActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
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

    private fun showHistory(show: Boolean) {
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        if (show) {
            clHistoryOverlay.visibility = android.view.View.VISIBLE
            clHistoryOverlay.translationX = -screenWidth
            clHistoryOverlay.animate()
                .translationX(0f)
                .setDuration(300)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        } else {
            clHistoryOverlay.animate()
                .translationX(-screenWidth)
                .setDuration(250)
                .setInterpolator(android.view.animation.AccelerateInterpolator())
                .withEndAction { clHistoryOverlay.visibility = android.view.View.GONE }
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}
