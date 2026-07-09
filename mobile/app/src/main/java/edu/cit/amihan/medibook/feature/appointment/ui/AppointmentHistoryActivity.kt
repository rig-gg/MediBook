package edu.cit.amihan.medibook.feature.appointment.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.databinding.ActivityAppointmentHistoryBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AppointmentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentHistoryBinding
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppointmentAdapter(emptyList())
        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter

        fetchAppointments()
    }

    override fun onResume() {
        super.onResume()
        fetchAppointments()
    }

    private fun fetchAppointments() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.appointmentApi.getMyAppointments()
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val appointments = response.body()!!
                    adapter.updateData(appointments)
                    binding.tvEmpty.visibility =
                        if (appointments.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                    binding.tvError.visibility = android.view.View.GONE
                } else if (response.code() == 401) {
                    showError("Session expired. Please log in again.")
                } else {
                    showError("Failed to load appointments (code ${response.code()}).")
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