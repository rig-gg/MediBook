package edu.cit.amihan.medibook.feature.schedule.model

import com.google.gson.annotations.SerializedName

data class DoctorSchedule(
    @SerializedName("scheduleId") val scheduleId: Long,
    @SerializedName("doctorId") val doctorId: Long,
    @SerializedName("doctorName") val doctorName: String,
    @SerializedName("specialization") val specialization: String?,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("isAvailable") val isAvailable: Boolean
)