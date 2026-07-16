package edu.cit.amihan.medibook.feature.appointment.network

import edu.cit.amihan.medibook.feature.appointment.model.AppointmentRequest
import edu.cit.amihan.medibook.feature.appointment.model.AppointmentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AppointmentApiService {

    @POST("api/appointments")
    suspend fun bookAppointment(@Body request: AppointmentRequest): Response<AppointmentResponse>

    @GET("api/appointments/me")
    suspend fun getMyAppointments(): Response<List<AppointmentResponse>>

    @PATCH("api/appointments/{id}/cancel")
    suspend fun cancelAppointment(@Path("id") appointmentId: Long): Response<AppointmentResponse>
}