package edu.cit.amihan.medibook.feature.appointment.model

import com.google.gson.annotations.SerializedName

data class FdaDrugSuggestion(
    @SerializedName("brandName") val brandName: String? = null,
    @SerializedName("genericName") val genericName: String? = null,
    @SerializedName("route") val route: String? = null,
    @SerializedName("indication") val indication: String? = null
)

data class HealthRecordResponse(
    @SerializedName("recordId") val recordId: Long = 0L,
    @SerializedName("appointmentId") val appointmentId: Long = 0L,
    @SerializedName("doctorName") val doctorName: String? = null,
    @SerializedName("patientName") val patientName: String? = null,
    @SerializedName("diagnosis") val diagnosis: String? = null,
    @SerializedName("consultationNotes") val consultationNotes: String? = null,
    @SerializedName("recordedAt") val recordedAt: String? = null,
    @SerializedName("fdaSuggestions") val fdaSuggestions: List<FdaDrugSuggestion>? = null
)
