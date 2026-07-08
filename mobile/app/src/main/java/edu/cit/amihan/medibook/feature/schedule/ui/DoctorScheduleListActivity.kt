package edu.cit.amihan.medibook.ui.schedule

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.databinding.ActivityDoctorScheduleListBinding
import edu.cit.amihan.medibook.model.AppointmentRequest
import kotlinx.coroutines.launch

class DoctorScheduleListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorScheduleListBinding
    private lateinit var adapter: ScheduleAdapter

    private var doctorId: Long = -1L
    private var doctorName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        doctorId = intent.getLongExtra("doctorId", -1L)
        doctorName = intent.getStringExtra("doctorName") ?: "Doctor"

        binding.tvDoctorHeader.text = "Available slots with Dr. $doctorName"

        adapter = ScheduleAdapter(emptyList()) { schedule ->
            confirmBooking(schedule.scheduleId, schedule.startTime)
        }
        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = adapter

        fetchSchedules()
    }

    private fun fetchSchedules() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.scheduleApi.getSchedules(doctorId)
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val schedules = response.body()!!
                    adapter.updateData(schedules)
                    binding.tvEmpty.visibility =
                        if (schedules.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tvError.visibility = android.view.View.GONE
                } else {
                    showError("Failed to load schedules (code ${response.code()}).")
                }
            } catch (e: Exception) {
                setLoading(false)
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun confirmBooking(scheduleId: Long, startTime: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Booking")
            .setMessage("Book appointment with Dr. $doctorName at $startTime?")
            .setPositiveButton("Book") { _, _ -> bookAppointment(scheduleId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bookAppointment(scheduleId: Long) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.appointmentApi.bookAppointment(
                    AppointmentRequest(scheduleId)
                )
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        this@DoctorScheduleListActivity,
                        "Appointment requested! Status: PENDING",
                        Toast.LENGTH_LONG
                    ).show()
                    fetchSchedules() // refresh so the now-booked slot disappears
                } else if (response.code() == 409 || response.code() == 400) {
                    showError("This slot was just booked by someone else. Please pick another.")
                    fetchSchedules()
                } else {
                    showError("Booking failed (code ${response.code()}).")
                }
            } catch (e: Exception) {
                setLoading(false)
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }
}