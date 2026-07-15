package edu.cit.amihan.medibook.feature.patient.network

import edu.cit.amihan.medibook.feature.patient.model.PatientResponse
import edu.cit.amihan.medibook.feature.patient.model.PatientRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface PatientApiService {

    @GET("api/patients/me")
    suspend fun getMyProfile(): Response<PatientResponse>

    @PUT("api/patients/me")
    suspend fun updateMyProfile(@Body request: PatientRequest): Response<PatientResponse>
}
