package edu.cit.amihan.medibook.feature.doctor.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityDoctorListBinding
import edu.cit.amihan.medibook.feature.appointment.ui.AppointmentHistoryActivity
import edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity
import edu.cit.amihan.medibook.feature.schedule.ui.DoctorScheduleListActivity
import kotlinx.coroutines.CancellationException
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

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    TokenManager.clear()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
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
            } catch (e: CancellationException) {
                throw e
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