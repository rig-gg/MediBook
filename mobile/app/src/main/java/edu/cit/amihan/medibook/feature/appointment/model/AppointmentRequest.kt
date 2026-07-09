package edu.cit.amihan.medibook.feature.appointment.model

import com.google.gson.annotations.SerializedName

data class AppointmentRequest(
    @SerializedName("scheduleId") val scheduleId: Long
)