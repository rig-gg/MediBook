package edu.cit.amihan.medibook.feature.doctor.network

import edu.cit.amihan.medibook.feature.doctor.model.Doctor
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DoctorApiService {

    @GET("api/doctors")
    suspend fun getDoctors(
        @Query("specialization") specialization: String? = null
    ): Response<List<Doctor>>
}