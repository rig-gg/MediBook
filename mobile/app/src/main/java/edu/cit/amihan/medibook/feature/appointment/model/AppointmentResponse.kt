package edu.cit.amihan.medibook.feature.appointment.model

import com.google.gson.annotations.SerializedName

data class AppointmentResponse(
    @SerializedName("appointmentId") val appointmentId: Long = 0L,
    @SerializedName("patientId") val patientId: Long = 0L,
    @SerializedName("patientName") val patientName: String? = null,
    @SerializedName("doctorId") val doctorId: Long = 0L,
    @SerializedName("doctorName") val doctorName: String? = null,
    @SerializedName("startTime") val startTime: String? = null,
    @SerializedName("endTime") val endTime: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null
)