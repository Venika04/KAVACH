package com.example.kavach.auth

import android.content.Intent
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    var user by mutableStateOf<FirebaseUser?>(auth.currentUser)
        private set

    fun loginWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                user = auth.currentUser
                onResult(it.isSuccessful)
            }
    }

    fun registerWithEmail(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                user = auth.currentUser
                onResult(it.isSuccessful)
            }
    }

    fun handleGoogleSignInResult(data: Intent?, onResult: (Boolean) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.result
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    user = auth.currentUser
                    onResult(it.isSuccessful)
                }
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In Failed", e)
            onResult(false)
        }
    }

    fun signOut() {
        auth.signOut()
        user = null
    }
}