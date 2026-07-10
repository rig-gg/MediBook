package edu.cit.amihan.medibook.feature.auth.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityLoginBinding
import edu.cit.amihan.medibook.feature.auth.model.LoginRequest
import edu.cit.amihan.medibook.feature.auth.ui.dashboard.DashboardActivity
import edu.cit.amihan.medibook.feature.auth.ui.register.RegisterActivity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login if a valid PATIENT token exists
        if (TokenManager.isLoggedIn() && TokenManager.getRole() == "PATIENT") {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        } else if (TokenManager.isLoggedIn()) {
            TokenManager.clear()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.")
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(username, password))
                setLoading(false)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!

                    if (authResponse.role != "PATIENT") {
                        showError("Mobile access is for PATIENT accounts only. Use the web portal.")
                        return@launch
                    }

                    TokenManager.saveSession(
                        token = authResponse.token ?: "",
                        userId = authResponse.userId,
                        fullName = authResponse.fullName ?: "Patient",
                        role = authResponse.role ?: ""
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome back, ${authResponse.fullName ?: "Patient"}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError(errorBody ?: "Invalid username or password.")
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
        binding.btnLogin.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }
}