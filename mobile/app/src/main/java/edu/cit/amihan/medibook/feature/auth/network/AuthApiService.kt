package edu.cit.amihan.medibook.feature.auth.network

import edu.cit.amihan.medibook.feature.auth.model.AuthResponse
import edu.cit.amihan.medibook.feature.auth.model.LoginRequest
import edu.cit.amihan.medibook.feature.auth.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register/patient")
    suspend fun registerPatient(@Body request: RegisterRequest): Response<AuthResponse>
}