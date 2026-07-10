package edu.cit.amihan.medibook.feature.auth.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token") val token: String? = null,
    @SerializedName("userId") val userId: Long = 0L,
    @SerializedName("username") val username: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("role") val role: String? = null
)