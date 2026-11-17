package edu.ap.dakaniktochniwegsmijten.data.user

data class UserProfile(
    val uid: String,
    val name: String,
    val email: String,
    val role: UserRole
)