package edu.cit.amihan.medibook.network

import edu.cit.amihan.medibook.model.DoctorSchedule
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScheduleApiService {

    @GET("api/schedules")
    suspend fun getSchedules(
        @Query("doctorId") doctorId: Long? = null
    ): Response<List<DoctorSchedule>>
}