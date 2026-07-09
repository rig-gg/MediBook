package edu.cit.amihan.medibook.feature.auth.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("role") val role: String
)