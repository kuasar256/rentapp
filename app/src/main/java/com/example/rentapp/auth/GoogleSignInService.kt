package com.example.rentapp.auth

import android.content.Context
import com.example.rentapp.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class GoogleSignInService(private val context: Context) {
    
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    data class GoogleSignInResult(
        val success: Boolean,
        val email: String? = null,
        val displayName: String? = null,
        val photoUrl: String? = null,
        val idToken: String? = null,
        val error: String? = null
    )
    
    fun getSignInClient(): GoogleSignInClient = googleSignInClient
    
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
    
    suspend fun checkLoggedInUser(): GoogleSignInResult = withContext(Dispatchers.Main) {
        try {
            val user = auth.currentUser
            if (user != null) {
                GoogleSignInResult(
                    success = true,
                    email = user.email,
                    displayName = user.displayName,
                    photoUrl = user.photoUrl?.toString()
                )
            } else {
                GoogleSignInResult(success = false, error = "No logged in user found")
            }
        } catch (e: Exception) {
            GoogleSignInResult(success = false, error = e.message ?: "Unknown error")
        }
    }
}
