package com.example.applockpro.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.applockpro.base.BaseActivity
import com.example.applockpro.databinding.ActivityPasswordBinding // Create this layout

class PasswordActivity : BaseActivity<ActivityPasswordBinding>() {

    private var CORRECT_PASSWORD = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CORRECT_PASSWORD = "${getStoredPassword()}"
        getBinding()?.let {
            with(it) {
                buttonSubmitPassword.setOnClickListener {
                    val enteredPassword = editTextPassword.text.toString()
                    if (enteredPassword == CORRECT_PASSWORD) {
                        // Password correct, mark as unlocked and proceed
                        setAppUnlocked(true)
                        navigateToMainApp()
                    } else {
                        showToast(message = "Incorrect Password")
                        editTextPassword.text.clear()
                    }
                }
            }
        }

    }

    override fun createBinding(): ActivityPasswordBinding {
        return ActivityPasswordBinding.inflate(layoutInflater)
    }




    // Prevent going back if password hasn't been entered
    override fun onBackPressed() {
        // You might want to allow exiting the app or show a confirm dialog
        // For simplicity, this example just minimizes the app
        moveTaskToBack(true)
    }

}
