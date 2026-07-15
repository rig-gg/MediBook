package edu.cit.amihan.medibook.feature.appointment.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Intent
import edu.cit.amihan.medibook.R
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityAppointmentHistoryBinding
import edu.cit.amihan.medibook.feature.appointment.model.AppointmentResponse
import edu.cit.amihan.medibook.feature.appointment.model.HealthRecordResponse
import edu.cit.amihan.medibook.feature.auth.ui.dashboard.DashboardActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AppointmentHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppointmentHistoryBinding
    private lateinit var adapter: AppointmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppointmentHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppointmentAdapter(emptyList()) { appt -> showDetailDialog(appt) }
        binding.rvAppointments.layoutManager = LinearLayoutManager(this)
        binding.rvAppointments.adapter = adapter

        binding.tvHome.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }
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
                        if (appointments.isEmpty()) View.VISIBLE else View.GONE
                    binding.tvError.visibility = View.GONE
                } else if (response.code() == 401) {
                    TokenManager.clear()
                    startActivity(Intent(this@AppointmentHistoryActivity,
                        edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
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

    private fun showDetailDialog(appt: AppointmentResponse) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_appointment_detail, null)

        view.findViewById<TextView>(R.id.tvDetailDoctor).text = appt.doctorName ?: "—"
        view.findViewById<TextView>(R.id.tvDetailTime).text = appt.startTime ?: "—"

        val statusTv = view.findViewById<TextView>(R.id.tvDetailStatus)
        statusTv.text = appt.status ?: ""
        when (appt.status) {
            "PENDING" -> {
                statusTv.background = ContextCompat.getDrawable(this, R.drawable.status_badge_pending)
                statusTv.setTextColor(ContextCompat.getColor(this, R.color.status_pending_text))
            }
            "CONFIRMED" -> {
                statusTv.background = ContextCompat.getDrawable(this, R.drawable.status_badge_confirmed)
                statusTv.setTextColor(ContextCompat.getColor(this, R.color.status_confirmed_text))
            }
            "COMPLETED" -> {
                statusTv.background = ContextCompat.getDrawable(this, R.drawable.status_badge_completed)
                statusTv.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text))
            }
            "CANCELLED" -> {
                statusTv.background = ContextCompat.getDrawable(this, R.drawable.status_badge_cancelled)
                statusTv.setTextColor(ContextCompat.getColor(this, R.color.status_cancelled_text))
            }
        }

        val progressRecord = view.findViewById<View>(R.id.progressRecord)
        val tvNoRecord = view.findViewById<TextView>(R.id.tvNoRecord)
        val layoutRecord = view.findViewById<LinearLayout>(R.id.layoutRecord)
        val layoutFda = view.findViewById<LinearLayout>(R.id.layoutFda)
        val layoutFdaItems = view.findViewById<LinearLayout>(R.id.layoutFdaItems)

        progressRecord.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.recordApi.getRecordByAppointment(appt.appointmentId)
                progressRecord.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val record = response.body()!!
                    showRecordInView(record, layoutRecord, layoutFda, layoutFdaItems, view)
                } else {
                    tvNoRecord.visibility = View.VISIBLE
                }
            } catch (_: CancellationException) {
                throw CancellationException()
            } catch (_: Exception) {
                progressRecord.visibility = View.GONE
                tvNoRecord.visibility = View.VISIBLE
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()
        dialog.window?.setBackgroundDrawableResource(R.color.medibook_surface)

        view.findViewById<TextView>(R.id.tvDetailClose).setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun showRecordInView(
        record: HealthRecordResponse,
        layoutRecord: LinearLayout,
        layoutFda: LinearLayout,
        layoutFdaItems: LinearLayout,
        root: View
    ) {
        layoutRecord.visibility = View.VISIBLE

        root.findViewById<TextView>(R.id.tvDetailDiagnosis).text = record.diagnosis ?: "—"

        val notesTv = root.findViewById<TextView>(R.id.tvDetailNotes)
        if (record.consultationNotes.isNullOrBlank()) {
            notesTv.visibility = View.GONE
        } else {
            notesTv.text = record.consultationNotes
        }

        root.findViewById<TextView>(R.id.tvDetailRecordedAt).text =
            "Recorded: ${record.recordedAt ?: "—"}"

        val suggestions = record.fdaSuggestions
        if (!suggestions.isNullOrEmpty()) {
            layoutFda.visibility = View.VISIBLE
            layoutFdaItems.removeAllViews()
            for (drug in suggestions) {
                val item = LayoutInflater.from(this).inflate(R.layout.item_fda_suggestion, layoutFdaItems, false)
                item.findViewById<TextView>(R.id.tvDrugName).text =
                    drug.brandName ?: drug.genericName ?: "Unknown Drug"

                val genericTv = item.findViewById<TextView>(R.id.tvDrugGeneric)
                if (!drug.brandName.isNullOrBlank() && !drug.genericName.isNullOrBlank()) {
                    genericTv.text = "Generic: ${drug.genericName}"
                    genericTv.visibility = View.VISIBLE
                }

                if (!drug.route.isNullOrBlank()) {
                    item.findViewById<TextView>(R.id.tvDrugRoute).apply {
                        text = "Route: ${drug.route}"
                        visibility = View.VISIBLE
                    }
                }

                if (!drug.indication.isNullOrBlank()) {
                    item.findViewById<TextView>(R.id.tvDrugIndication).apply {
                        text = drug.indication
                        visibility = View.VISIBLE
                    }
                }

                layoutFdaItems.addView(item)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}
