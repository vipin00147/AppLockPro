package com.example.applockpro.service // Or a suitable package

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.applockpro.ui.activity.AppLockScreenActivity
import com.example.applockpro.utils.biomatricUtility.BiometricUtility.checkBioMetric

class AppLockService : AccessibilityService() {

    private lateinit var sharedPrefs: SharedPreferences
    private var lockedAppsSet: Set<String> = mutableSetOf()
    private var currentForegroundApp: String? = null

    companion object {
        private const val TAG = "AppLockService"
        // To prevent re-locking immediately after unlocking
        private val temporarilyUnlockedApps = mutableSetOf<String>()
        private const val TEMP_UNLOCK_DURATION_MS = 5000L // 5 seconds, adjust as needed

        fun temporarilyUnlockApp(packageName: String?) {
            packageName?.let {
                temporarilyUnlockedApps.add(it)
                // You might want a Handler to remove it after a delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    temporarilyUnlockedApps.remove(it)
                }, TEMP_UNLOCK_DURATION_MS)
            }
        }
    }


    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility Service Connected")
        val serviceInfo = AccessibilityServiceInfo()
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        serviceInfo.notificationTimeout = 100
        this.serviceInfo = serviceInfo

        sharedPrefs = getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        loadLockedApps()

        // Listen for changes in SharedPreferences (if you change locked apps while service is running)
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsChangedListener)
    }

    private val prefsChangedListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "locked_apps") {
            loadLockedApps()
        }
    }

    private fun loadLockedApps() {
        lockedAppsSet = sharedPrefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
        Log.d(TAG, "Loaded locked apps: $lockedAppsSet")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()

            if (packageName != null && className != null) {
                 // Ignore your own app's lock screen and your app's main UI to prevent loops
                if (packageName == applicationContext.packageName &&
                    (className.contains("AppLockScreenActivity") || className.contains("PasswordPromptDialogFragment"))) {
                    return
                }
                // Also ignore if the launcher itself is the foreground app (prevents locking the launcher)
                if (isLauncherApp(packageName)) {
                    currentForegroundApp = packageName
                    return
                }


                if (packageName != currentForegroundApp) {
                    currentForegroundApp = packageName
                    Log.d(TAG, "Foreground app changed to: $packageName, Class: $className")

                    if (lockedAppsSet.contains(packageName) && !temporarilyUnlockedApps.contains(packageName) && packageName != applicationContext.packageName) {
                        Log.d(TAG, "Found locked app in foreground: $packageName. Showing lock screen.")
                        showLockScreen(packageName)
                    }
                }
            }
        }
    }

    private fun isLauncherApp(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = packageManager.resolveActivity(intent, 0)
        return resolveInfo != null && resolveInfo.activityInfo != null && packageName == resolveInfo.activityInfo.packageName
    }


    private fun showLockScreen(lockedAppPackage: String) {
        val appLockScreenClassName = AppLockScreenActivity::class.java.name // Get the full class name
        if (currentForegroundApp == appLockScreenClassName) {

            Log.d(TAG, "Lock screen ($appLockScreenClassName) is already the foreground. Not relaunching for $lockedAppPackage.")
            return
        }

        Log.d(TAG, "Attempting to show lock screen for: $lockedAppPackage")
        val launchIntent = Intent(this, AppLockScreenActivity::class.java)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        launchIntent.putExtra(AppLockScreenActivity.EXTRA_LOCKED_APP_PACKAGE_NAME, lockedAppPackage)

        try {
            startActivity(launchIntent)
            Log.d(TAG, "Successfully started AppLockScreenActivity for $lockedAppPackage")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AppLockScreenActivity for $lockedAppPackage", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Accessibility Service Unbound")
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(prefsChangedListener)
        return super.onUnbind(intent)
    }
}
