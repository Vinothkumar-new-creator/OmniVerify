package com.omniverify

import com.google.gson.annotations.SerializedName

data class VerifyResponse(
    @SerializedName("status") val status: String? = null,
    // The backend sends ai_generated at the root level
    @SerializedName("ai_generated") val ai_generated: Double? = 0.0,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("markers") val markers: List<String>? = null
)
