package edu.cit.amihan.medibook.feature.schedule.model

import com.google.gson.annotations.SerializedName

data class DoctorSchedule(
    @SerializedName("scheduleId") val scheduleId: Long = 0L,
    @SerializedName("doctorId") val doctorId: Long = 0L,
    @SerializedName("doctorName") val doctorName: String? = null,
    @SerializedName("specialization") val specialization: String? = null,
    @SerializedName("startTime") val startTime: String? = null,
    @SerializedName("endTime") val endTime: String? = null,
    @SerializedName("isAvailable") val isAvailable: Boolean = false
)