package edu.cit.amihan.medibook.model

data class Doctor(
    val doctorId: Long,
    val fullName: String,
    val specialization: String?,
    val contactNumber: String?,
    val email: String
)