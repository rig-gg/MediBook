package edu.cit.amihan.medibook.feature.appointment.network

import edu.cit.amihan.medibook.feature.appointment.model.HealthRecordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface RecordApiService {

    @GET("api/records/appointment/{appointmentId}")
    suspend fun getRecordByAppointment(
        @Path("appointmentId") appointmentId: Long
    ): Response<HealthRecordResponse>
}
