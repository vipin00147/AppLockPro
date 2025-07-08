package com.example.applockpro.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    var isLocked : Boolean = false
)