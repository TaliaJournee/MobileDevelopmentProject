package edu.ap.dakaniktochniwegsmijten.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    fun signOut() {
        auth.signOut()
    }
}