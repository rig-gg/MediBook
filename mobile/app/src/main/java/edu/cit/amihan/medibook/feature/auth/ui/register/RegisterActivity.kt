package edu.cit.amihan.medibook.feature.auth.ui.register

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityRegisterBinding
import edu.cit.amihan.medibook.feature.auth.model.RegisterRequest
import edu.cit.amihan.medibook.feature.doctor.ui.DoctorListActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etDateOfBirth.setOnClickListener { showDatePicker() }

        binding.btnRegister.setOnClickListener { attemptRegister() }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                binding.etDateOfBirth.setText(formatted)
            },
            calendar.get(Calendar.YEAR) - 25,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun attemptRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val dateOfBirth = binding.etDateOfBirth.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in full name, username, email, and password.")
            return
        }
        if (password.length < 8) {
            showError("Password must be at least 8 characters.")
            return
        }
        if (password != confirmPassword) {
            showError("Passwords do not match.")
            return
        }

        setLoading(true)

        val request = RegisterRequest(
            username = username,
            password = password,
            email = email,
            fullName = fullName,
            dateOfBirth = dateOfBirth.ifEmpty { null },
            contactNumber = contactNumber.ifEmpty { null },
            address = address.ifEmpty { null }
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.registerPatient(request)
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    TokenManager.saveSession(
                        token = authResponse.token,
                        userId = authResponse.userId,
                        fullName = authResponse.fullName,
                        role = authResponse.role
                    )

                    Toast.makeText(
                        this@RegisterActivity,
                        "Welcome, ${authResponse.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@RegisterActivity, DoctorListActivity::class.java))
                    finish()
                } else {
                    showError(response.errorBody()?.string() ?: "Registration failed. Please try again.")
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
        binding.btnRegister.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }
}