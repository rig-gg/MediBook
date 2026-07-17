package edu.cit.amihan.medibook.core.network

import edu.cit.amihan.medibook.feature.auth.network.AuthApiService
import edu.cit.amihan.medibook.feature.appointment.network.AppointmentApiService
import edu.cit.amihan.medibook.feature.appointment.network.RecordApiService
import edu.cit.amihan.medibook.feature.doctor.network.DoctorApiService
import edu.cit.amihan.medibook.feature.patient.network.PatientApiService
import edu.cit.amihan.medibook.feature.schedule.network.ScheduleApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val USE_LOCAL = false   // set to false for production

    private val BASE_URL = if (USE_LOCAL) {
        "http://10.0.2.2:8080/"   // local backend (emulator)
    } else {
        "https://medibook-api.onrender.com/"   // production
    }

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
    val recordApi: RecordApiService = retrofit.create(RecordApiService::class.java)
    val patientApi: PatientApiService = retrofit.create(PatientApiService::class.java)
}