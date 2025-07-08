package com.example.applockpro.ui.bottomSheet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.applockpro.base.BottomSheetBase
import com.example.applockpro.databinding.FragmentSettingsBottomSheetBinding
import com.example.applockpro.ui.activity.ChangePasswordActivity
import com.example.applockpro.utils.biomatricUtility.BiometricUtility.checkBioMetric

class SettingsBottomSheet : BottomSheetBase<FragmentSettingsBottomSheetBinding>() {

    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentSettingsBottomSheetBinding {
        return FragmentSettingsBottomSheetBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        getBinding()?.let {
            with(it) {

                val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
                val biometricEnabled = sharedPrefs.getBoolean("biometric_enabled", false)

                btnSwitchBiomatric.isChecked = biometricEnabled

                btnChangePassword.setOnClickListener {
                    val intent = Intent(getBaseActivity(), ChangePasswordActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                btnSwitchBiomatric.setOnCheckedChangeListener { _, isChecked ->

                    if(isChecked) {
                        enableBioMatric()
                    } else {

                        checkBioMetric(
                            baseActivity = getBaseActivity(),
                            onFailed = object : () -> Unit {
                                override fun invoke() {
                                    enableBioMatric()
                                }
                            },
                            onSuccess = object : () -> Unit {
                                override fun invoke() {
                                    disableBioMatric()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private fun disableBioMatric() {

        val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("biometric_enabled", false).apply()
    }


    private fun enableBioMatric() {
        val sharedPrefs = getBaseActivity().getSharedPreferences("AppLockPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("biometric_enabled", true).apply()
        getBinding()?.btnSwitchBiomatric?.isChecked = true
    }

}