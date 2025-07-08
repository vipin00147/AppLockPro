package com.example.applockpro.ui.activity // Or a suitable package

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.applockpro.base.BaseActivity
import com.example.applockpro.databinding.ActivityAppLockScreenBinding // Create this layout
import com.example.applockpro.service.AppLockService
import com.example.applockpro.utils.biomatricUtility.BiometricUtility.checkBioMetric

class AppLockScreenActivity : BaseActivity<ActivityAppLockScreenBinding>() {

    private var CORRECT_APP_PASSWORD = ""
    private var lockedAppPackage: String? = null

    companion object {
        const val EXTRA_LOCKED_APP_PACKAGE_NAME = "locked_app_package_name"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CORRECT_APP_PASSWORD = "${getStoredPassword()}"
        lockedAppPackage = intent.getStringExtra(EXTRA_LOCKED_APP_PACKAGE_NAME)
        if (lockedAppPackage == null) {
            finish()
            return
        }


        getBinding()?.buttonSubmitLockPassword?.setOnClickListener {
            val enteredPassword = getBinding()?.editTextLockPassword?.text.toString()
            if (enteredPassword == CORRECT_APP_PASSWORD) {
                // Password correct, allow access
                AppLockService.temporarilyUnlockApp(lockedAppPackage) // Notify service
                finish() // Close the lock screen
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
                getBinding()?.editTextLockPassword?.text?.clear()
            }
        }

        val sharedPrefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        val biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false)

        if(biometricEnabled) {

            checkBioMetric(
                baseActivity = this,
                onFailed = object : () -> Unit {
                    override fun invoke() {
                        /**  Do Nothing Here*/
                    }
                },
                onSuccess = object : () -> Unit {
                    override fun invoke() {
                        AppLockService.temporarilyUnlockApp(lockedAppPackage) // Notify service
                        finish()
                    }
                }
            )
        } else {
            /**  Do Nothing Here*/
        }

        try {
            getBinding()?.textViewLockedAppName?.text = "${getAppNameFromPackage(packageName = lockedAppPackage)}"
            getBinding()?.ivLoackedAppIcon?.let { Glide.with(this).load(getAppIconFromPackage(packageName = lockedAppPackage)).into(it) }
        } catch (_: Exception) { }

    }

    override fun createBinding(): ActivityAppLockScreenBinding {
        return ActivityAppLockScreenBinding.inflate(layoutInflater)
    }

    // Prevent bypassing the lock screen by pressing back
    override fun onBackPressed() {
        // This is a common strategy: send user to home screen if they try to back out
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Finish the lock screen so it doesn't stay in the back stack
    }

    // Helper to get app name (optional, for display)

    private fun getAppNameFromPackage(packageName: String?): String {
        return try {
            val packageManager = applicationContext.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName!!, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName ?: "App"
        }
    }

    private fun getAppIconFromPackage(packageName: String?): Drawable? {
        return try {
            val packageManager = applicationContext.packageManager
            packageManager.getApplicationIcon(packageName!!)
        } catch (e: PackageManager.NameNotFoundException) {
            // Optionally, return a default icon or handle the error as needed
            null // Or return a default/placeholder drawable
        }
    }
}
