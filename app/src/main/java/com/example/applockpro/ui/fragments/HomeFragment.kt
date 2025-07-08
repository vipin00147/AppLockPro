package com.example.applockpro.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.applockpro.R
import com.example.applockpro.adapter.AppListAdapter
import com.example.applockpro.base.BaseFragment
import com.example.applockpro.callback.HomeFragmentCallback
import com.example.applockpro.databinding.FragmentHomeFragmentBinding
import com.example.applockpro.model.AppInfo
import com.example.applockpro.ui.bottomSheet.SettingsBottomSheet
import com.example.applockpro.utils.AppLister
import com.example.applockpro.utils.biomatricUtility.BiometricUtility.checkBioMetric

class HomeFragment : BaseFragment<FragmentHomeFragmentBinding>(), HomeFragmentCallback {

    private var appListAdapter: AppListAdapter? = null
    private val installedApps = ArrayList<AppInfo>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentHomeFragmentBinding {
        return FragmentHomeFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAppRecyclerView()

        getBinding()?.let {
            with(it) {
                searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextChange(p0: String?): Boolean {
                        performSearch(searchText = p0.toString().trim())
                        return true
                    }

                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }
                })

                btnOption.setOnClickListener {
                    changePasswordScreen()
                }
            }
        }
    }

    private fun initAppRecyclerView() {

        if (appListAdapter == null) {
            getBinding()?.let {
                with(it) {

                    appListAdapter = AppListAdapter(
                        baseActivity = getBaseActivity(),
                        onCheckChange = object : (Boolean, Int, AppInfo) -> Unit {
                            override fun invoke(isChecked: Boolean, position: Int, appData : AppInfo) {

                                /*when(getBaseActivity().isBiometricEnabled()) {
                                    true -> {
                                        checkBioMetric(
                                            baseActivity = getBaseActivity(),
                                            onFailed = object : () -> Unit {
                                                override fun invoke() {
                                                    lockUnlockApp(isLocked = false, appData = appData)
                                                }
                                            },
                                            onSuccess = object : () -> Unit {
                                                override fun invoke() {
                                                    lockUnlockApp(isLocked = isChecked, appData = appData)
                                                }
                                            }
                                        )
                                    }
                                    else -> {
                                        lockUnlockApp(isLocked = isChecked, appData = appData)
                                    }
                                }*/

                                lockUnlockApp(isLocked = isChecked, appData = appData)

                            }
                        }
                    )

                    rvAppsList.adapter = appListAdapter

                    val divider = DividerItemDecoration(getBaseActivity(), DividerItemDecoration.VERTICAL)
                    ContextCompat.getDrawable(getBaseActivity(), R.drawable.chat_users_item_divider)
                        ?.let {
                            divider.setDrawable(it)
                        }
                    rvAppsList.addItemDecoration(divider)

                    val lockedAppsPackageList = getLockedApps()
                    val appLister = AppLister(context = getBaseActivity())
                    installedApps.clear()
                    installedApps.addAll(appLister.getInstalledApps())
                    appListAdapter?.updateList(items = installedApps)

                    getBinding()?.tvTotalPackages?.text = "Total Packeges : ${installedApps.size}"

                    installedApps.map {
                        it.isLocked = lockedAppsPackageList.contains(it.packageName)
                    }.apply {
                        appListAdapter?.updateList(items = installedApps)
                    }
                }
            }
        }
    }

    override fun lockUnlockApp(isLocked : Boolean, appData : AppInfo, notifyItem : Boolean) {
        if (isLocked) {

            val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
            val lockedAppsSet = sharedPrefs.getStringSet("locked_apps", mutableSetOf()) ?: mutableSetOf()
            lockedAppsSet.add(appData.packageName)
            sharedPrefs.edit().putStringSet("locked_apps", lockedAppsSet).apply()
            if(notifyItem) {
                appListAdapter?.lockUnlockApp(isLocked, appData)
            }


        } else {

            val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
            val lockedAppsSet = sharedPrefs.getStringSet("locked_apps", mutableSetOf()) ?: mutableSetOf()
            lockedAppsSet.remove(appData.packageName)
            sharedPrefs.edit().putStringSet("locked_apps", lockedAppsSet).apply()
            if(notifyItem) {
                appListAdapter?.lockUnlockApp(isLocked, appData)
            }
        }
    }

    fun getLockedApps(): Set<String> {
        val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        return sharedPrefs.getStringSet("locked_apps", emptySet()) ?: emptySet()
    }

    private fun performSearch(searchText : String) {
        installedApps.filter { it.appName.contains(searchText, ignoreCase = true) }.apply {
            appListAdapter?.updateList(items = this)
        }
    }

    fun changePasswordScreen() {
        val settingsBottomSheet = SettingsBottomSheet()
        settingsBottomSheet.show(getBaseActivity().supportFragmentManager, "")
    }

    override fun onResume() {
        super.onResume()

        getBaseActivity().homeFragmentCallback = this
    }
}