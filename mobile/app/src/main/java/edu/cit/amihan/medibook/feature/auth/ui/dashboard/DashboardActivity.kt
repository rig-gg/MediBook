package edu.cit.amihan.medibook.feature.auth.ui.dashboard

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityDashboardBinding
import edu.cit.amihan.medibook.feature.appointment.ui.AppointmentHistoryActivity
import edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity
import edu.cit.amihan.medibook.feature.doctor.ui.DoctorListActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fullName = TokenManager.getFullName()
        binding.tvWelcome.text = "Welcome back${if (!fullName.isNullOrBlank()) ", $fullName" else ""}."

        loadAppointmentCount()

        binding.cardBrowseDoctors.setOnClickListener {
            startActivity(Intent(this, DoctorListActivity::class.java))
        }

        binding.cardMyAppointments.setOnClickListener {
            startActivity(Intent(this, AppointmentHistoryActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    TokenManager.clear()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadAppointmentCount() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.appointmentApi.getMyAppointments()
                if (response.isSuccessful && response.body() != null) {
                    val appointments = response.body()!!
                    val upcoming = appointments.count { it.status == "PENDING" || it.status == "CONFIRMED" }
                    binding.tvAppointmentCount.text = "$upcoming"
                } else {
                    binding.tvAppointmentCount.text = "0"
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                binding.tvAppointmentCount.text = "\u2014"
            }
        }
    }
}
