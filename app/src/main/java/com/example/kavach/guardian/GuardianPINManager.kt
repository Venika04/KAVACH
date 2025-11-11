package com.example.kavach.guardian

import android.content.Context
import android.content.SharedPreferences

object GuardianPINManager {
    private const val PREF_NAME = "guardian_pin_prefs"
    private const val KEY_PIN = "guardian_pin"

    fun setPIN(context: Context, pin: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun getPIN(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PIN, null)
    }

    fun isPINSet(context: Context): Boolean {
        return getPIN(context) != null
    }

    fun validatePIN(context: Context, enteredPIN: String): Boolean {
        return getPIN(context) == enteredPIN
    }

    fun clearPIN(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PIN).apply()
    }
}
