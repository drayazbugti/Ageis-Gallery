package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

enum class LockType {
  PIN,
  PASSWORD,
  PATTERN
}

sealed class AuthResult {
  data object SuccessMaster : AuthResult()
  data object SuccessDecoy : AuthResult()
  data class Error(val message: String) : AuthResult()
}

class VaultSecurityManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("aegis_vault_secure_prefs", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_LOCK_TYPE = "lock_type"
    private const val KEY_MASTER_HASH = "master_hash"
    private const val KEY_DECOY_HASH = "decoy_hash"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_CALCULATOR_DISGUISE = "calculator_disguise"
    private const val KEY_AUTO_LOCK_SECONDS = "auto_lock_seconds"
    private const val KEY_VAULT_INITIALIZED = "vault_initialized"
    private const val KEY_CUSTOM_PIN_CONFIGURED = "custom_pin_configured"

    const val DEFAULT_PIN = "1234"
    const val DEFAULT_DECOY_PIN = "0000"
    private const val DEFAULT_PATTERN = "0,1,2,5,8" // L-shape pattern
  }

  init {
    if (!prefs.getBoolean(KEY_VAULT_INITIALIZED, false)) {
      prefs.edit()
        .putString(KEY_LOCK_TYPE, LockType.PIN.name)
        .putString(KEY_MASTER_HASH, hashSecret(DEFAULT_PIN))
        .putString(KEY_DECOY_HASH, hashSecret(DEFAULT_DECOY_PIN))
        .putBoolean(KEY_BIOMETRIC_ENABLED, true)
        .putBoolean(KEY_CALCULATOR_DISGUISE, false)
        .putInt(KEY_AUTO_LOCK_SECONDS, 60)
        .putBoolean(KEY_CUSTOM_PIN_CONFIGURED, false)
        .putBoolean(KEY_VAULT_INITIALIZED, true)
        .commit()
    }
  }

  fun isCustomPinConfigured(): Boolean = prefs.getBoolean(KEY_CUSTOM_PIN_CONFIGURED, false)

  fun getLockType(): LockType {
    val name = prefs.getString(KEY_LOCK_TYPE, LockType.PIN.name) ?: LockType.PIN.name
    return try {
      LockType.valueOf(name)
    } catch (e: Exception) {
      LockType.PIN
    }
  }

  fun setLockType(type: LockType, newSecret: String): Boolean {
    return prefs.edit()
      .putString(KEY_LOCK_TYPE, type.name)
      .putString(KEY_MASTER_HASH, hashSecret(newSecret.trim()))
      .putBoolean(KEY_CUSTOM_PIN_CONFIGURED, true)
      .commit()
  }

  fun saveNewMasterPin(newPin: String): Boolean {
    if (newPin.trim().length < 4) return false
    return prefs.edit()
      .putString(KEY_LOCK_TYPE, LockType.PIN.name)
      .putString(KEY_MASTER_HASH, hashSecret(newPin.trim()))
      .putBoolean(KEY_CUSTOM_PIN_CONFIGURED, true)
      .commit()
  }

  fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
  fun setBiometricEnabled(enabled: Boolean): Boolean =
    prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).commit()

  fun isCalculatorDisguiseEnabled(): Boolean =
    prefs.getBoolean(KEY_CALCULATOR_DISGUISE, false)
  fun setCalculatorDisguiseEnabled(enabled: Boolean): Boolean =
    prefs.edit().putBoolean(KEY_CALCULATOR_DISGUISE, enabled).commit()

  fun setDecoySecret(secret: String): Boolean {
    return prefs.edit().putString(KEY_DECOY_HASH, hashSecret(secret.trim())).commit()
  }

  fun verifySecret(secret: String): AuthResult {
    val inputHash = hashSecret(secret.trim())
    val masterHash = prefs.getString(KEY_MASTER_HASH, "") ?: ""
    val decoyHash = prefs.getString(KEY_DECOY_HASH, "") ?: ""

    return when {
      inputHash == masterHash -> AuthResult.SuccessMaster
      decoyHash.isNotEmpty() && inputHash == decoyHash -> AuthResult.SuccessDecoy
      else -> AuthResult.Error("Incorrect PIN/Password. Please try again.")
    }
  }

  private fun hashSecret(secret: String): String {
    val salt = "aegis_vault_salt_2026_#"
    val bytes = (secret + salt).toByteArray(Charsets.UTF_8)
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    return digest.joinToString("") { "%02x".format(it) }
  }
}
