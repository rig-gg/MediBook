package edu.cit.amihan.medibook.network

import edu.cit.amihan.medibook.model.AuthResponse
import edu.cit.amihan.medibook.model.LoginRequest
import edu.cit.amihan.medibook.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register/patient")
    suspend fun registerPatient(@Body request: RegisterRequest): Response<AuthResponse>
}