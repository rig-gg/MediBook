package edu.cit.amihan.medibook.feature.patient.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityProfileEditBinding
import edu.cit.amihan.medibook.feature.patient.model.PatientRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        loadProfile()

        binding.btnSave.setOnClickListener { saveProfile() }
    }

    private fun loadProfile() {
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.patientApi.getMyProfile()
                setLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val patient = response.body()!!
                    binding.etFullName.setText(patient.fullName ?: "")
                    binding.etContactNumber.setText(patient.contactNumber ?: "")
                    binding.etAddress.setText(patient.address ?: "")
                    binding.tvEmail.text = patient.email ?: ""
                } else if (response.code() == 401) {
                    TokenManager.clear()
                    startActivity(intentFor<edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity>().apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    showError("Failed to load profile (code ${response.code()}).")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLoading(false)
                showError("Network error: ${e.message}")
            }
        }
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        if (fullName.isBlank()) {
            binding.etFullName.error = "Full name is required"
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val request = PatientRequest(
                    fullName = fullName,
                    contactNumber = binding.etContactNumber.text.toString().trim().ifBlank { null },
                    address = binding.etAddress.text.toString().trim().ifBlank { null }
                )
                val response = RetrofitClient.patientApi.updateMyProfile(request)
                setLoading(false)
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileEditActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                } else if (response.code() == 401) {
                    TokenManager.clear()
                    startActivity(intentFor<edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity>().apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    showError("Failed to save profile (code ${response.code()}).")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setLoading(false)
                showError("Network error: ${e.message}")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> intentFor(): android.content.Intent =
        android.content.Intent(this, Class.forName("edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity"))

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
}
