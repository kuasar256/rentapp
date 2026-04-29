package com.example.rentapp.auth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    private const val TAG = "BiometricHelper"

    /**
     * Verifica si el dispositivo soporta autenticación biométrica o por PIN/contraseña.
     */
    fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.w(TAG, "No hay biometría registrada en el dispositivo")
                false
            }
            else -> false
        }
    }

    /**
     * Verifica si el dispositivo tiene soporte de hardware biométrico (huella/cara).
     * Devuelve false si solo tiene PIN.
     */
    fun hasBiometricHardware(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
    }

    /**
     * Lanza el diálogo de autenticación biométrica.
     *
     * @param activity La actividad actual (requerida por BiometricPrompt).
     * @param title Título del diálogo.
     * @param subtitle Subtítulo opcional.
     * @param onSuccess Callback cuando la autenticación es exitosa.
     * @param onError Callback cuando hay un error o el usuario cancela.
     * @param onFailed Callback cuando la biometría no coincide (sin cancelar).
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Verificar identidad",
        subtitle: String = "Use su huella dactilar o cara para acceder",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Autenticación biométrica exitosa")
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.w(TAG, "Error biométrico [$errorCode]: $errString")
                onError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(TAG, "Autenticación biométrica fallida (biometría no coincide)")
                onFailed()
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            // Permite PIN/patrón como alternativa si la biometría falla
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
