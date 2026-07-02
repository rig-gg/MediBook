package edu.cit.amihan.medibook.model

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val fullName: String,
    val dateOfBirth: String? = null,   // format: "yyyy-MM-dd"
    val contactNumber: String? = null,
    val address: String? = null
)