package edu.cit.amihan.medibook.feature.doctor.model

import com.google.gson.annotations.SerializedName

data class Doctor(
    @SerializedName("doctorId") val doctorId: Long,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("specialization") val specialization: String?,
    @SerializedName("contactNumber") val contactNumber: String?,
    @SerializedName("email") val email: String
)