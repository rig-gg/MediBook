package edu.cit.amihan.medibook.ui.doctor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.amihan.medibook.databinding.ActivityDoctorListBinding
import edu.cit.amihan.medibook.network.RetrofitClient
import edu.cit.amihan.medibook.ui.appointment.AppointmentHistoryActivity
import edu.cit.amihan.medibook.ui.schedule.DoctorScheduleListActivity
import kotlinx.coroutines.launch

class DoctorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorListBinding
    private lateinit var adapter: DoctorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DoctorAdapter(emptyList()) { doctor ->
            val intent = Intent(this, DoctorScheduleListActivity::class.java)
            intent.putExtra("doctorId", doctor.doctorId)
            intent.putExtra("doctorName", doctor.fullName)
            startActivity(intent)
        }
        binding.rvDoctors.layoutManager = LinearLayoutManager(this)
        binding.rvDoctors.adapter = adapter

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearchSpecialization.text.toString().trim()
            fetchDoctors(query)
        }

        binding.btnMyAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentHistoryActivity::class.java))
        }

        fetchDoctors()
    }

    private fun fetchDoctors(specialization: String? = null) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.doctorApi.getDoctors(
                    specialization?.ifEmpty { null }
                )
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val doctors = response.body()!!
                    adapter.updateData(doctors)
                    binding.tvEmpty.visibility =
                        if (doctors.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tvError.visibility = android.view.View.GONE
                } else if (response.code() == 401) {
                    showError("Session expired. Please log in again.")
                } else {
                    showError("Failed to load doctors (code ${response.code()}).")
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