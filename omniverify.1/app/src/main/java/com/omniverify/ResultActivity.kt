package com.omniverify

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val isAi = intent.getBooleanExtra("isAi", true)
        val confidence = intent.getIntExtra("confidence", 78)
        val summary = intent.getStringExtra("summary") ?: if (isAi) {
            "Patterns suggest AI-generated imagery with synthetic visual markers."
        } else {
            "Image analysis indicates authentic human-captured content with natural characteristics."
        }
        val markers = intent.getStringArrayListExtra("markers") ?: arrayListOf()

        val btnClose = findViewById<ImageView>(R.id.btnClose)
        val ivResultIcon = findViewById<ImageView>(R.id.ivResultIcon)
        val tvConfidencePercent = findViewById<TextView>(R.id.tvConfidencePercent)
        val pbConfidence = findViewById<ProgressBar>(R.id.pbConfidence)
        val tvResultBadge = findViewById<TextView>(R.id.tvResultBadge)
        val tvSummaryText = findViewById<TextView>(R.id.tvSummaryText)
        val btnScanAgain = findViewById<Button>(R.id.btnScanAgain)
        val tvBackToHome = findViewById<TextView>(R.id.tvBackToHome)

        val tvMarker1 = findViewById<TextView>(R.id.tvMarker1)
        val tvMarker2 = findViewById<TextView>(R.id.tvMarker2)
        val tvMarker3 = findViewById<TextView>(R.id.tvMarker3)

        btnClose.setOnClickListener { finish() }
        tvBackToHome.setOnClickListener { finish() }
        btnScanAgain.setOnClickListener { finish() }

        // Set values
        tvConfidencePercent.text = "$confidence%"
        pbConfidence.progress = confidence
        tvSummaryText.text = summary

        if (isAi) {
            setupAiUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers)
        } else {
            setupHumanUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers)
        }
    }

    private fun setupAiUI(
        icon: ImageView, badge: TextView, 
        button: Button, m1: TextView, m2: TextView, m3: TextView,
        markers: List<String>
    ) {
        val red = ContextCompat.getColor(this, R.color.result_red)
        icon.setImageResource(R.drawable.ic_close)
        icon.setColorFilter(red)
        
        badge.text = "AI Generated"
        badge.setTextColor(red)
        badge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_red_translucent)
        badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shield, 0, 0, 0)
        badge.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.result_red)

        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_red)

        val markerViews = listOf(m1, m2, m3)
        markerViews.forEachIndexed { index, textView ->
            if (index < markers.size) {
                textView.text = markers[index]
                textView.visibility = android.view.View.VISIBLE
                textView.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.result_red)
            } else {
                textView.visibility = android.view.View.GONE
            }
        }
    }

    private fun setupHumanUI(
        icon: ImageView, badge: TextView, 
        button: Button, m1: TextView, m2: TextView, m3: TextView,
        markers: List<String>
    ) {
        val green = ContextCompat.getColor(this, R.color.active_green)
        icon.setImageResource(R.drawable.ic_check)
        icon.setColorFilter(green)
        
        badge.text = "Human Made"
        badge.setTextColor(green)
        badge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_green_translucent)
        badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shield, 0, 0, 0)
        badge.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.active_green)

        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.active_green)

        val markerViews = listOf(m1, m2, m3)
        markerViews.forEachIndexed { index, textView ->
            if (index < markers.size) {
                textView.text = markers[index]
                textView.visibility = android.view.View.VISIBLE
                textView.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.active_green)
            } else {
                textView.visibility = android.view.View.GONE
            }
        }
    }
}
