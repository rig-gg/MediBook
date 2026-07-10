package edu.cit.amihan.medibook

import android.app.Application
import android.content.Intent
import edu.cit.amihan.medibook.core.network.AuthInterceptor
import edu.cit.amihan.medibook.core.utils.TokenManager
import edu.cit.amihan.medibook.feature.auth.ui.login.LoginActivity

class MedibookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)

        // Global 401 handler: clear token and redirect to LoginActivity
        AuthInterceptor.onUnauthorized = {
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
        }
    }
}