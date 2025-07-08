package com.example.applockpro.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.applockpro.base.KEY_PASSWORD
import com.example.applockpro.base.PREFS_NAME
import com.example.applockpro.utils.biomatricUtility.BiometricUtility.checkBioMetric
import java.util.concurrent.Executor

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isAppUnlocked()) {
            Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "aaa")
            navigateToMainApp()
        } else {
            if(getStoredPassword().isNullOrEmpty()) {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "bbb")
                navigateToMainApp()
            } else {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "ccc")
                val sharedPrefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
                val biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false)

                if(biometricEnabled) {

                    checkBioMetric(
                        baseActivity = this,
                        onFailed = object : () -> Unit {
                            override fun invoke() {
                                openPasswordInputActivity()
                            }
                        },
                        onSuccess = object : () -> Unit {
                            override fun invoke() {
                                navigateToMainApp()
                            }
                        }
                    )
                } else {
                    navigateToPasswordScreen()
                }
            }
        }
    }

    private fun isAppUnlocked(): Boolean {
        // Check if the app was previously unlocked in this session
        // For simplicity, we'll use SharedPreferences.
        // For more robust session management, consider other techniques.
        val sharedPrefs = getSharedPreferences("AppLockProInternalPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean("is_app_unlocked", false)
    }

    private fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java) // Replace with your main activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToPasswordScreen() {
        val intent = Intent(this, PasswordActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    fun getStoredPassword(): String? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // In a real app, you'd be comparing a hash
        return sharedPrefs.getString(KEY_PASSWORD, "")
    }

    private fun openPasswordInputActivity() {
        navigateToPasswordScreen()
    }
}