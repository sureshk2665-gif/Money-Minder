package com.moneyminder.app.util

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

object MpinManager {
    private const val PREFS_NAME = "mpin_prefs"
    private const val KEY_MPIN_HASH = "mpin_hash"
    private const val KEY_SECURITY_QUESTION = "security_question"
    private const val KEY_SECURITY_ANSWER_HASH = "security_answer_hash"
    private const val KEY_MPIN_ENABLED = "mpin_enabled"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
    private const val KEY_LOCKOUT_UNTIL = "lockout_until"
    private const val MAX_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isMpinSet(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_MPIN_ENABLED, false)

    fun setupMpin(context: Context, pin: String, question: String, answer: String) {
        getPrefs(context).edit()
            .putString(KEY_MPIN_HASH, hashPin(pin))
            .putString(KEY_SECURITY_QUESTION, question)
            .putString(KEY_SECURITY_ANSWER_HASH, hashPin(answer.trim().lowercase()))
            .putBoolean(KEY_MPIN_ENABLED, true)
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .apply()
    }

    fun verifyMpin(context: Context, pin: String): Boolean {
        val prefs = getPrefs(context)
        val stored = prefs.getString(KEY_MPIN_HASH, null) ?: return false
        val match = hashPin(pin) == stored
        if (match) {
            prefs.edit().putInt(KEY_FAILED_ATTEMPTS, 0).apply()
        } else {
            val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, attempts)
            if (attempts >= MAX_ATTEMPTS) {
                editor.putLong(KEY_LOCKOUT_UNTIL, System.currentTimeMillis() + LOCKOUT_DURATION_MS)
            }
            editor.apply()
        }
        return match
    }

    fun getFailedAttempts(context: Context): Int =
        getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)

    fun getLockoutUntil(context: Context): Long =
        getPrefs(context).getLong(KEY_LOCKOUT_UNTIL, 0L)

    fun isLockedOut(context: Context): Boolean =
        System.currentTimeMillis() < getLockoutUntil(context)

    fun getRemainingLockoutSeconds(context: Context): Int {
        val remaining = getLockoutUntil(context) - System.currentTimeMillis()
        return if (remaining > 0) (remaining / 1000).toInt() + 1 else 0
    }

    fun clearLockout(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun getSecurityQuestion(context: Context): String =
        getPrefs(context).getString(KEY_SECURITY_QUESTION, "") ?: ""

    fun verifySecurityAnswer(context: Context, answer: String): Boolean {
        val stored = getPrefs(context).getString(KEY_SECURITY_ANSWER_HASH, null) ?: return false
        return hashPin(answer.trim().lowercase()) == stored
    }

    fun resetMpin(context: Context, newPin: String) {
        getPrefs(context).edit()
            .putString(KEY_MPIN_HASH, hashPin(newPin))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun resetMpinWithSecurityQuestion(context: Context, newPin: String, newQuestion: String, newAnswer: String) {
        getPrefs(context).edit()
            .putString(KEY_MPIN_HASH, hashPin(newPin))
            .putString(KEY_SECURITY_QUESTION, newQuestion)
            .putString(KEY_SECURITY_ANSWER_HASH, hashPin(newAnswer.trim().lowercase()))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }
}
