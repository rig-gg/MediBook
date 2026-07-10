package edu.cit.amihan.medibook.feature.doctor.model

import com.google.gson.annotations.SerializedName

data class Doctor(
    @SerializedName("doctorId") val doctorId: Long = 0L,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("email") val email: String? = null
)