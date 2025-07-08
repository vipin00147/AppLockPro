package com.example.applockpro.ui.bottomSheet

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.applockpro.R
import com.example.applockpro.adapter.AppListAdapter
import com.example.applockpro.base.BottomSheetBase
import com.example.applockpro.databinding.FragmentLockedAppsBinding
import com.example.applockpro.model.AppInfo
import com.example.applockpro.utils.AppLister

class LockedAppsBottomSheet : BottomSheetBase<FragmentLockedAppsBinding>() {

    private var appListAdapter: AppListAdapter? = null
    private val installedApps = ArrayList<AppInfo>()

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentLockedAppsBinding {
        return FragmentLockedAppsBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        Handler(Looper.getMainLooper()).postDelayed({
            initAppRecyclerView()
        },300)


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
                                getBaseActivity().homeFragmentCallback?.lockUnlockApp(isLocked = isChecked, appData = appData, notifyItem = true)
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

                    val list  = appLister.getInstalledApps().filter { lockedAppsPackageList.contains(it.packageName) }
                    installedApps.clear()
                    installedApps.addAll(list)

                    appListAdapter?.updateList(items = installedApps)

                    getBinding()?.tvTotalPackages?.text = "Total Locked Apps : ${installedApps.size}"

                    installedApps.map {
                        it.isLocked = lockedAppsPackageList.contains(it.packageName)
                    }.apply {
                        appListAdapter?.updateList(items = installedApps)
                    }
                }
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

}