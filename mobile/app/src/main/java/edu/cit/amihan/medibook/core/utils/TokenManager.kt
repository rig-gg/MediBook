package edu.cit.amihan.medibook.core.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREFS_NAME = "medibook_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_FULL_NAME = "full_name"
    private const val KEY_ROLE = "role"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(token: String, fullName: String, role: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_FULL_NAME, fullName)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)

    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun clear() {
        prefs.edit().clear().apply()
    }
}