package edu.cit.amihan.medibook.feature.auth.network

import edu.cit.amihan.medibook.feature.auth.model.AuthResponse
import edu.cit.amihan.medibook.feature.auth.model.LoginRequest
import edu.cit.amihan.medibook.feature.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class RefreshTokenRequest(val refreshToken: String)
data class LogoutRequest(val refreshToken: String?)

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register/patient")
    suspend fun registerPatient(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(
        @Body request: LogoutRequest,
        @Header("Authorization") authHeader: String? = null
    ): Response<String>
}
