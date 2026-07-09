package edu.cit.amihan.medibook.core.network

import edu.cit.amihan.medibook.feature.auth.network.AuthApiService
import edu.cit.amihan.medibook.feature.appointment.network.AppointmentApiService
import edu.cit.amihan.medibook.feature.doctor.network.DoctorApiService
import edu.cit.amihan.medibook.feature.schedule.network.ScheduleApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (android.util.Log.isLoggable("Retrofit", android.util.Log.DEBUG))
            HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)
    val doctorApi: DoctorApiService = retrofit.create(DoctorApiService::class.java)
    val scheduleApi: ScheduleApiService = retrofit.create(ScheduleApiService::class.java)
    val appointmentApi: AppointmentApiService = retrofit.create(AppointmentApiService::class.java)
}