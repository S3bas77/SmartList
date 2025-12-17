package com.example.smartlist

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("SmartListPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_EMAIL = "user_email"
        const val IS_LOGGED_IN = "is_logged_in"
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit().putString(USER_EMAIL, email).apply()
    }

    fun getUserEmail(): String? {
        return prefs.getString(USER_EMAIL, null)
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(IS_LOGGED_IN, loggedIn).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}