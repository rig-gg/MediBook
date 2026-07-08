package edu.cit.amihan.medibook.model

data class DoctorSchedule(
    val scheduleId: Long,
    val doctorId: Long,
    val doctorName: String,
    val specialization: String?,
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean
)