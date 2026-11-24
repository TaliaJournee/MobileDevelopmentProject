package edu.ap.dakaniktochniwegsmijten.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import edu.ap.dakaniktochniwegsmijten.data.user.UserProfile
import edu.ap.dakaniktochniwegsmijten.data.user.UserRole

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

    suspend fun createAccount(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): UserProfile {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()

        require(trimmedName.length in 1..100) {
            "Name must contain between 1 and 100 characters."
        }
        require(trimmedEmail.length in 1..254) {
            "Email must contain between 1 and 254 characters."
        }

        val result = auth.createUserWithEmailAndPassword(
            trimmedEmail,
            password
        ).await()

        val user = checkNotNull(result.user) {
            "Account creation completed without a user."
        }
        val accountEmail = checkNotNull(user.email) {
            "The new email and password account has no email address."
        }

        return UserProfile(
            uid = user.uid,
            name = trimmedName,
            email = accountEmail,
            role = role
        )
    }

    fun signOut() {
        auth.signOut()
    }
}