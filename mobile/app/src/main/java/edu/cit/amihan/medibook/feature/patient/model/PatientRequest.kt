package edu.cit.amihan.medibook.feature.patient.model

import com.google.gson.annotations.SerializedName

data class PatientRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("address") val address: String? = null
)
