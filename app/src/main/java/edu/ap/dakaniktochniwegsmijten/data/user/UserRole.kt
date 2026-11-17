package edu.ap.dakaniktochniwegsmijten.data.user

enum class UserRole(val firestoreValue: String) {
    CUSTOMER("customer"),
    MERCHANT("merchant");

    companion object {
        fun fromFirestore(value: String?): UserRole? {
            return entries.firstOrNull { role ->
                role.firestoreValue == value
            }
        }
    }
}