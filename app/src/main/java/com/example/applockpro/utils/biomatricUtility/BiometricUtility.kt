package com.example.applockpro.utils.biomatricUtility

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

object BiometricUtility {

    private var executor: Executor ?= null
    private var biometricPrompt: BiometricPrompt ?= null
    private var promptInfo: BiometricPrompt.PromptInfo ?= null

    fun checkBioMetric(baseActivity: AppCompatActivity,
                       onFailed : () -> Unit,
                       onSuccess : () -> Unit
                       ) {
        executor = ContextCompat.getMainExecutor(baseActivity)

        // Check for biometric availability
        val biometricManager = BiometricManager.from(baseActivity)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Biometric authentication is available and user has enrolled.
                // You can choose to directly show the biometric prompt here,
                // or offer it as an option alongside password input.
                Log.d("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "App can authenticate using biometrics.")
                setupBiometricPrompt(baseActivity = baseActivity, onFailed = onFailed, onSuccess = onSuccess) // Prepare the biometric prompt
                // Decide when to show it (e.g., on a button click, or automatically on app start)
                // For example, if you want to show it immediately:
                promptInfo?.let { biometricPrompt?.authenticate(it) }
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "No biometric features available on this device.")
                // Proceed to password input activity
                onFailed.invoke()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Biometric features are currently unavailable.")
                // Proceed to password input activity
                onFailed.invoke()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // The user hasn't associated any biometric credentials with their account.
                // Prompts the user to create credentials that your app accepts.
                // You might want to guide the user to system settings to enroll biometrics.
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "User hasn't enrolled any biometrics.")
                // Proceed to password input activity, maybe with a message suggesting fingerprint enrollment.
                onFailed.invoke()

                // Optionally, you can prompt the user to enroll biometrics:
                // val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                //     putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                //         BiometricManager.Authenticators.BIOMETRIC_STRONG)
                // }
                // startActivityForResult(enrollIntent, YOUR_REQUEST_CODE_BIOMETRIC_ENROLL)
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Biometric security update required.")
                onFailed.invoke()
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Biometric authentication is unsupported.")
                onFailed.invoke()
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Biometric status is unknown.")
                onFailed.invoke()
            }
        }
    }

    private fun setupBiometricPrompt(
        baseActivity: AppCompatActivity,
        onFailed : () -> Unit,
        onSuccess : () -> Unit
    ) {
        biometricPrompt = executor?.let {
            BiometricPrompt(baseActivity, it,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Authentication error: $errString")
                        // If the user cancels or there's an error, fall back to password
                        // Be careful not to create a loop if password activity also tries biometrics.
                        // You might need a flag to prevent re-triggering biometrics from password activity.
                        onFailed.invoke()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        // Authentication was successful.
                        // Proceed to your app's main content or unlock the app.
                        onSuccess.invoke()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        onFailed.invoke()
                        Log.e("dkscbsdcbsdjcbsdbcksjndcbsndkbcndscnsdcdscdskcdschsdbcjdscsddsc", "Authentication failed")
                        // User's fingerprint not recognized. The prompt will typically stay visible.
                    }
                })
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for AppLock Pro")
            .setSubtitle("Log in using your fingerprint")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }
}