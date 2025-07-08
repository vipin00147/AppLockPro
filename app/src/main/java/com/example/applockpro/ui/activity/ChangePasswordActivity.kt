package com.example.applockpro.ui.activity // Adjust package name as needed

import android.content.Context
import android.os.Bundle
import android.view.View
import com.example.applockpro.base.BaseActivity
import com.example.applockpro.databinding.ActivityChangePasswordBinding // Import ViewBinding class
import com.example.applockpro.hideView
import com.example.applockpro.showView


private const val MIN_PASSWORD_LENGTH = 4 // Example minimum length

class ChangePasswordActivity : BaseActivity<ActivityChangePasswordBinding>() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.title = "Change Password"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show back button

        handleChangePassword()

        getBinding()?.let {
            with(it) {
                btnSaveChanges.setOnClickListener {
                    handleChangePassword()
                }

                backIcon.setOnClickListener {
                    super.onBackPressed()
                }
            }
        }

    }

    override fun createBinding(): ActivityChangePasswordBinding {
        return ActivityChangePasswordBinding.inflate(layoutInflater)
    }

    private fun handleChangePassword() {

        getBinding()?.let {
            with(it) {
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmNewPassword = etConfirmNewPassword.text.toString()

                // --- Verify Current Password ---
                val storedPassword = getStoredPassword()

                // --- Basic Validation ---

                if(storedPassword.isNullOrEmpty()) {
                    if (newPassword.isEmpty()) {
                        tilCurrentPassword.hideView()
                        tilConfirmNewPassword.hideView()
                        return
                    }
                } else {
                    if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                        tilCurrentPassword.showView()
                        tilConfirmNewPassword.showView()
                        showToast("All fields are required.")
                        return
                    }
                }


                if(!storedPassword.isNullOrEmpty()) {
                    if (newPassword.length < MIN_PASSWORD_LENGTH) {
                        tilNewPassword.error = "Password must be at least $MIN_PASSWORD_LENGTH characters."
                        return
                    } else {
                        tilNewPassword.error = null // Clear error
                    }

                    if (newPassword != confirmNewPassword) {
                        tilConfirmNewPassword.error = "New passwords do not match."
                        return
                    } else {
                        tilConfirmNewPassword.error = null // Clear error
                    }

                }

                // --- Simulate loading ---
                showLoading(true)



                if(storedPassword.isNullOrEmpty()) {
                    tilCurrentPassword.hideView()
                    tilConfirmNewPassword.hideView()
                } else {
                    tilCurrentPassword.showView()
                    tilConfirmNewPassword.showView()
                }

                if(!currentPassword.isNullOrEmpty()) {
                    if (currentPassword != storedPassword) {
                        tilCurrentPassword.error = "Incorrect current password."
                        showLoading(false)
                        return
                    } else {
                        tilCurrentPassword.error = null // Clear error
                    }
                }


                // --- Save New Password ---
                savePassword(newPassword)
                showLoading(false)
                showToast("Password changed successfully!")
                finish() // Close the activity
            }
        }


    }

    private fun showLoading(isLoading: Boolean) {
        getBinding()?.let {
            with(it) {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnSaveChanges.isEnabled = !isLoading
                etCurrentPassword.isEnabled = !isLoading
                etNewPassword.isEnabled = !isLoading
                etConfirmNewPassword.isEnabled = !isLoading
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed() // Handle back button press
        return true
    }
}
