package com.omniverify

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val verdict = intent.getStringExtra("verdict") ?: (if (intent.getBooleanExtra("isAi", true)) "AI" else "HUMAN")
        val confidence = intent.getIntExtra("confidence", 78)
        val summary = intent.getStringExtra("summary") ?: when (verdict) {
            "AI" -> "Patterns suggest AI-generated imagery with synthetic visual markers."
            "PARTIAL_AI" -> "Analysis shows potential AI-generated elements mixed with human characteristics."
            "DANGEROUS", "DANGEROUS_SCRIPT" -> "This content contains security threats and is flagged as dangerous."
            else -> "Analysis indicates authentic human-captured content with natural characteristics."
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
        val clResultCard = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.clResultCard)

        val llMarkers = findViewById<LinearLayout>(R.id.llMarkers)
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

        if (markers.isEmpty()) {
            llMarkers.visibility = android.view.View.GONE
        } else {
            llMarkers.visibility = android.view.View.VISIBLE
        }

        when (verdict) {
            "AI", "DANGEROUS", "DANGEROUS_SCRIPT" -> {
                setupAiUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers, verdict, clResultCard)
            }
            "PARTIAL_AI", "LIKELY_AI" -> {
                setupPartialAiUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers, clResultCard)
            }
            "HUMAN" -> {
                setupHumanUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers, clResultCard)
            }
            else -> {
                setupHumanUI(ivResultIcon, tvResultBadge, btnScanAgain, tvMarker1, tvMarker2, tvMarker3, markers, clResultCard)
            }
        }
    }

    private fun setupPartialAiUI(
        icon: ImageView, badge: TextView,
        button: Button, m1: TextView, m2: TextView, m3: TextView,
        markers: List<String>, card: androidx.constraintlayout.widget.ConstraintLayout
    ) {
        val yellow = ContextCompat.getColor(this, R.color.result_yellow)
        icon.setImageResource(R.drawable.ic_shield)
        icon.setColorFilter(yellow)

        badge.text = getString(R.string.partial_ai)
        badge.setTextColor(yellow)
        badge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_yellow_translucent)
        badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shield, 0, 0, 0)
        badge.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.result_yellow)

        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_yellow)
        card.setBackgroundResource(R.drawable.bg_result_card_partial)

        updateMarkers(listOf(m1, m2, m3), markers, yellow)
    }

    private fun setupAiUI(
        icon: ImageView, badge: TextView, 
        button: Button, m1: TextView, m2: TextView, m3: TextView,
        markers: List<String>, verdict: String,
        card: androidx.constraintlayout.widget.ConstraintLayout
    ) {
        val red = ContextCompat.getColor(this, R.color.result_red)
        icon.setImageResource(R.drawable.ic_close)
        icon.setColorFilter(red)
        
        badge.text = if (verdict == "AI") getString(R.string.ai_generated) else getString(R.string.dangerous_content)
        badge.setTextColor(red)
        badge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_red_translucent)
        badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shield, 0, 0, 0)
        badge.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.result_red)

        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_red)
        card.setBackgroundResource(R.drawable.bg_result_card_ai)

        updateMarkers(listOf(m1, m2, m3), markers, red)
    }

    private fun setupHumanUI(
        icon: ImageView, badge: TextView, 
        button: Button, m1: TextView, m2: TextView, m3: TextView,
        markers: List<String>,
        card: androidx.constraintlayout.widget.ConstraintLayout
    ) {
        val green = ContextCompat.getColor(this, R.color.active_green)
        icon.setImageResource(R.drawable.ic_check)
        icon.setColorFilter(green)
        
        badge.text = getString(R.string.human_made)
        badge.setTextColor(green)
        badge.backgroundTintList = ContextCompat.getColorStateList(this, R.color.result_green_translucent)
        badge.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shield, 0, 0, 0)
        badge.compoundDrawableTintList = ContextCompat.getColorStateList(this, R.color.active_green)

        button.backgroundTintList = ContextCompat.getColorStateList(this, R.color.active_green)
        card.setBackgroundResource(R.drawable.bg_result_card_human)

        updateMarkers(listOf(m1, m2, m3), markers, green)
    }

    private fun updateMarkers(views: List<TextView>, markers: List<String>, color: Int) {
        views.forEachIndexed { index, textView ->
            if (index < markers.size) {
                textView.text = markers[index]
                textView.visibility = android.view.View.VISIBLE
                textView.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(color)
            } else {
                textView.visibility = android.view.View.GONE
            }
        }
    }
}
