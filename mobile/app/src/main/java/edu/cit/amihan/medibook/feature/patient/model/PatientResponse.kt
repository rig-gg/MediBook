package edu.cit.amihan.medibook.feature.patient.model

import com.google.gson.annotations.SerializedName

data class PatientResponse(
    @SerializedName("patientId") val patientId: Long = 0L,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("dateOfBirth") val dateOfBirth: String? = null,
    @SerializedName("contactNumber") val contactNumber: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("email") val email: String? = null
)
