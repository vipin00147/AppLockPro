package com.example.applockpro.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.example.applockpro.model.AppInfo

class AppLister(private val context: Context) {

    /*fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val appInfoList = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }

        for (resolveInfo in resolveInfoList) {
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(packageManager).toString()
                val appIcon = resolveInfo.loadIcon(packageManager)
                appInfoList.add(AppInfo(packageName, appName, appIcon))
            } catch (e: Exception) {
                // Handle cases where app info might be inaccessible
                // For example, log the error or skip the app
                e.printStackTrace()
            }
        }
        return appInfoList.sortedBy { it.appName.lowercase() } // Sort alphabetically
    }*/

    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager
        val appInfoList = mutableListOf<AppInfo>()

        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.MATCH_UNINSTALLED_PACKAGES.toLong()) // Or other flags as needed
            )
        } else {
            @Suppress("DEPRECATION") // Suppress for older SDKs
            packageManager.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES) // Or other flags
        }

        for (app in installedApps) {
            // Filter out system apps if needed
            // if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0) { // Example: Only non-system apps
            try {
                val appName = packageManager.getApplicationLabel(app).toString()
                val appIcon = packageManager.getApplicationIcon(app.packageName)
                // Assuming your AppInfo model can take packageName, appName, appIcon
                appInfoList.add(AppInfo(app.packageName, appName, appIcon))
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                // Handle cases where app info might be inaccessible
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // }
        }
        return appInfoList.sortedBy { it.appName.lowercase() }
    }
}
