package edu.cit.amihan.medibook.feature.doctor.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityDoctorListBinding
import edu.cit.amihan.medibook.feature.appointment.ui.AppointmentHistoryActivity
import edu.cit.amihan.medibook.feature.auth.ui.dashboard.DashboardActivity
import edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity
import edu.cit.amihan.medibook.feature.doctor.model.Doctor
import edu.cit.amihan.medibook.feature.schedule.ui.DoctorScheduleListActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DoctorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorListBinding
    private lateinit var adapter: DoctorAdapter
    private var allDoctors: List<Doctor> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DoctorAdapter(emptyList()) { doctor ->
            val intent = Intent(this, DoctorScheduleListActivity::class.java)
            intent.putExtra("doctorId", doctor.doctorId)
            intent.putExtra("doctorName", doctor.fullName ?: "")
            startActivity(intent)
        }
        binding.rvDoctors.layoutManager = LinearLayoutManager(this)
        binding.rvDoctors.adapter = adapter

        binding.btnMyAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentHistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    val token = TokenManager.getToken()
                    val refreshToken = TokenManager.getRefreshToken()
                    lifecycleScope.launch {
                        try {
                            RetrofitClient.authApi.logout(
                                request = edu.cit.amihan.medibook.feature.auth.network.LogoutRequest(refreshToken),
                                authHeader = if (!token.isNullOrEmpty()) "Bearer $token" else null
                            )
                        } catch (_: Exception) { }
                    }
                    TokenManager.clear()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.tvHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }

        fetchDoctors()
    }

    private fun fetchDoctors() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.doctorApi.getDoctors()
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    allDoctors = response.body()!!
                    adapter.updateData(allDoctors)
                    binding.tvEmpty.visibility =
                        if (allDoctors.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tvError.visibility = android.view.View.GONE
                    setupSpecializationDropdown()
                } else if (response.code() == 401) {
                    TokenManager.clear()
                    startActivity(Intent(this@DoctorListActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
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

    private fun setupSpecializationDropdown() {
        val specializations = allDoctors
            .mapNotNull { it.specialization?.ifBlank { null } }
            .distinct()
            .sorted()
            .toMutableList()
        specializations.add(0, "All Specializations")

        val dropdownAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            specializations
        )
        binding.actSpecialization.setAdapter(dropdownAdapter)
        binding.actSpecialization.setText("All Specializations", false)

        binding.actSpecialization.setOnItemClickListener { _, _, position, _ ->
            val selected = specializations[position]
            if (selected == "All Specializations") {
                adapter.updateData(allDoctors)
            } else {
                adapter.updateData(allDoctors.filter { it.specialization == selected })
            }
            binding.tvEmpty.visibility =
                if (adapter.itemCount == 0) android.view.View.VISIBLE else android.view.View.GONE
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