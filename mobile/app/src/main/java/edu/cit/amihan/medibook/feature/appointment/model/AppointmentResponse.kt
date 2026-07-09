package edu.cit.amihan.medibook.feature.appointment.model

import com.google.gson.annotations.SerializedName

data class AppointmentResponse(
    @SerializedName("appointmentId") val appointmentId: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("patientName") val patientName: String,
    @SerializedName("doctorId") val doctorId: Long,
    @SerializedName("doctorName") val doctorName: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String
)