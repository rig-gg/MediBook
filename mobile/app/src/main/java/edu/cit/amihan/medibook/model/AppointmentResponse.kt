package edu.cit.amihan.medibook.model

data class AppointmentResponse(
    val appointmentId: Long,
    val patientId: Long,
    val patientName: String,
    val doctorId: Long,
    val doctorName: String,
    val startTime: String,
    val endTime: String,
    val status: String,
    val createdAt: String
)