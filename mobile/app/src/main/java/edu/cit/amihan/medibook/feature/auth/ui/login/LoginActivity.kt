package edu.cit.amihan.medibook.feature.auth.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import edu.cit.amihan.medibook.core.network.RetrofitClient
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.databinding.ActivityLoginBinding
import edu.cit.amihan.medibook.feature.auth.model.LoginRequest
import edu.cit.amihan.medibook.feature.doctor.ui.DoctorListActivity
import edu.cit.amihan.medibook.ui.register.RegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                    TokenManager.saveSession(
                        token = authResponse.token,
                        fullName = authResponse.fullName,
                        role = authResponse.role
                    )

                    Toast.makeText(
                        this@LoginActivity,
                        "Welcome back, ${authResponse.fullName}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Doctor List is the current landing screen post-login.
                    // Swap this for a real dashboard Activity once one exists.
                    startActivity(Intent(this@LoginActivity, DoctorListActivity::class.java))
                    finish()
                } else {
                    showError("Invalid username or password.")
                }
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