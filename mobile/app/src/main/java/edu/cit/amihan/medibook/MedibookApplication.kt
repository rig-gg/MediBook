package edu.cit.amihan.medibook

import android.app.Application
import edu.cit.amihan.medibook.core.utils.TokenManager

class MedibookApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenManager.init(this)
    }
}