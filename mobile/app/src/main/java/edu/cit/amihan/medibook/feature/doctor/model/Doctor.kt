package edu.cit.amihan.medibook.feature.doctor.model

data class Doctor(
    val doctorId: Long,
    val fullName: String,
    val specialization: String?,
    val contactNumber: String?,
    val email: String
)