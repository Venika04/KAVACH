package com.example.kavach.guardian

import android.content.Context
import android.content.SharedPreferences

object GuardianPINManager {

    private const val PREF_NAME = "guardian_pin_prefs"
    private const val KEY_PIN = "guardian_pin"
    private const val DEFAULT_PIN = "0000"

    // Save PIN
    fun setPIN(context: Context, pin: String) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    // Get PIN (returns default if not set)
    fun getPIN(context: Context): String {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    // Check if user has set a custom PIN
    fun isPINSet(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        return prefs.contains(KEY_PIN)
    }

    // Validate PIN (safe, never null)
    fun validatePIN(context: Context, enteredPIN: String): Boolean {
        return getPIN(context) == enteredPIN
    }

    // Remove PIN (resets to default automatically)
    fun clearPIN(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        prefs.edit().remove(KEY_PIN).apply()
    }

    // 🔥 IMPORTANT: Ensure default PIN exists
    fun ensureDefaultPIN(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        if (!prefs.contains(KEY_PIN)) {
            prefs.edit().putString(KEY_PIN, DEFAULT_PIN).apply()
        }
    }
}
