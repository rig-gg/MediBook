package edu.cit.amihan.medibook.feature.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("address") val address: String? = null
)