package com.omniverify

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scanType: String, // IMAGE, TEXT, LINK, QR
    val rawContent: String,
    val verdict: String, // HUMAN, AI, PARTIAL_AI, DANGEROUS, SAFE
    val confidence: Int,
    val timestamp: Long = System.currentTimeMillis()
)