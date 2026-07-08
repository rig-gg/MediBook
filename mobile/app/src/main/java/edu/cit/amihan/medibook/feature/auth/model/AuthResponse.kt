package edu.cit.amihan.medibook.feature.auth.model

data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val fullName: String,
    val role: String
)