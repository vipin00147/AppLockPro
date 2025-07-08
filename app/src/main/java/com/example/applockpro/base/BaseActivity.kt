package com.example.applockpro.base

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import com.example.applockpro.callback.HomeFragmentCallback
import com.example.applockpro.service.AppLockService
import com.example.applockpro.ui.activity.MainActivity

const val PREFS_NAME = "AppLockProPrefs"
const val KEY_PASSWORD = "app_password"

abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {

    private var viewDataBinding: T? = null

    var homeFragmentCallback : HomeFragmentCallback?= null

    override fun onCreate(savedInstanceState: Bundle?) {

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)

        viewDataBinding = createBinding()
        setContentView(viewDataBinding?.root)

    }

    fun getBinding() = viewDataBinding

    abstract fun createBinding(): T

    fun getBaseActivity(): BaseActivity<ViewDataBinding> = this as BaseActivity<ViewDataBinding>

    fun showToast(message : String) {
        Toast.makeText(this, "$message", Toast.LENGTH_SHORT).show()
    }

    fun navigateToMainApp() {
        val intent = Intent(this, MainActivity::class.java) // Replace with your main activity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Finish PasswordActivity so user can't go back to it
    }

    fun setAppUnlocked(unlocked: Boolean) {
        val sharedPrefs = getSharedPreferences("AppLockProInternalPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_app_unlocked", unlocked).apply()
    }


    /** Enable this before going line*/
    override fun onStop() {
        super.onStop()
        setAppUnlocked(unlocked = false)
    }

    override fun onResume() {
        super.onResume()

        startLockService()
    }


    fun startLockService() {
        if (!isAccessibilityServiceEnabled(getBaseActivity(), AppLockService::class.java)) {

            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            } catch (_: Exception) {
                showToast(message = "Could not open Accessibility Settings. Please open them manually.")
            }

        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<out AccessibilityService>): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        am?.let {
            val enabledServices = it.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
            for (enabledService in enabledServices) {
                val enabledServiceInfo = enabledService.resolveInfo.serviceInfo
                if (enabledServiceInfo.packageName == context.packageName && enabledServiceInfo.name == service.name) {
                    return true
                }
            }
        }
        return false
    }

    fun savePassword(password: String) {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // In a real app, HASH the password before saving
        sharedPrefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun getStoredPassword(): String? {
        val sharedPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // In a real app, you'd be comparing a hash
        return sharedPrefs.getString(KEY_PASSWORD, "")
    }

    fun isBiometricEnabled() : Boolean {
        val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
       return sharedPrefs.getBoolean("biometric_enabled", false)

    }

}