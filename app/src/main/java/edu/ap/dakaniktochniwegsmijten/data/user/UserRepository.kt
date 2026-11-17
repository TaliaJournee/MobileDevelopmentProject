package edu.ap.dakaniktochniwegsmijten.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getProfile(uid: String): UserProfile? {
        require(uid.isNotBlank()) {
            "User ID must not be blank."
        }

        val document = firestore.collection("users")
            .document(uid)
            .get(Source.SERVER)
            .await()

        if (!document.exists()) {
            return null
        }

        val name = document.getString("name")
        val email = document.getString("email")
        val role = UserRole.fromFirestore(document.getString("role"))

        check(!name.isNullOrBlank() && name.length <= 100) {
            "User profile contains an invalid name."
        }
        check(!email.isNullOrBlank() && email.length <= 254) {
            "User profile contains an invalid email."
        }
        checkNotNull(role) {
            "User profile contains an invalid role."
        }

        return UserProfile(
            uid = document.id,
            name = name,
            email = email,
            role = role
        )
    }

    suspend fun createProfile(profile: UserProfile) {
        require(profile.uid.isNotBlank()) {
            "User ID must not be blank."
        }

        val name = profile.name.trim()
        val email = profile.email.trim()

        require(name.isNotBlank() && name.length <= 100) {
            "Name must contain between 1 and 100 characters."
        }
        require(email.isNotBlank() && email.length <= 254) {
            "Email must contain between 1 and 254 characters."
        }

        val documentReference = firestore.collection("users")
            .document(profile.uid)

        val data = mapOf(
            "name" to name,
            "email" to email,
            "role" to profile.role.firestoreValue
        )

        firestore.runTransaction { transaction ->
            val existingDocument = transaction.get(documentReference)

            check(!existingDocument.exists()) {
                "User profile already exists."
            }

            transaction.set(documentReference, data)
            Unit
        }.await()
    }
}