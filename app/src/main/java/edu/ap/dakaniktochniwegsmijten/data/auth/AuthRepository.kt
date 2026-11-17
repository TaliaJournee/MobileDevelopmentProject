package edu.ap.dakaniktochniwegsmijten.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun observeUserId(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }.distinctUntilChanged()

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()
    }

    suspend fun createAccount(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        return checkNotNull(result.user) {
            "Account creation completed without a user."
        }.uid
    }

    fun signOut() {
        auth.signOut()
    }
}