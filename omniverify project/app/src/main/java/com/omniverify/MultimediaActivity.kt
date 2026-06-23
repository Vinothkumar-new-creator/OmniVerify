package com.omniverify

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.omniverify.databinding.ActivityMultimediaBinding

import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MultimediaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMultimediaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMultimediaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNavigation.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupBottomNavigation()
        setupClickListeners()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.isItemActiveIndicatorEnabled = false
        binding.bottomNavigation.selectedItemId = R.id.nav_multimedia
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_multimedia -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.cardImage.setOnClickListener { openScanner("IMAGE") }
        binding.cardText.setOnClickListener { openScanner("TEXT") }
        binding.cardLink.setOnClickListener { openScanner("LINK") }
        binding.cardQR.setOnClickListener { openScanner("QR") }
    }

    private fun openScanner(type: String) {
        val intent = Intent(this, ScannerActivity::class.java)
        intent.putExtra("SCAN_TYPE", type)
        startActivity(intent)
    }
}