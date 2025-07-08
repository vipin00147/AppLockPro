package com.example.applockpro.callback

import com.example.applockpro.model.AppInfo

interface HomeFragmentCallback {
    fun lockUnlockApp(isLocked : Boolean, appData : AppInfo, notifyItem : Boolean = false)
}