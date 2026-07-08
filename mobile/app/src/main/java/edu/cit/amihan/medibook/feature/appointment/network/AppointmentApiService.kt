package edu.cit.amihan.medibook.network

import edu.cit.amihan.medibook.model.AppointmentRequest
import edu.cit.amihan.medibook.model.AppointmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AppointmentApiService {

    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

    @GET("api/appointments/me")
    suspend fun getMyAppointments(): Response<List<AppointmentResponse>>
}