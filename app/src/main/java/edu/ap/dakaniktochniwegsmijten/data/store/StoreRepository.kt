package edu.ap.dakaniktochniwegsmijten.data.store

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

class StoreAlreadyExistsException : Exception("The merchant already has a store.")

class StoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createStore(
        ownerId: String,
        name: String,
        category: String,
        latitude: Double,
        longitude: Double
    ) {
        val storeName = name.trim()
        val storeCategory = category.trim()

        require(ownerId.isNotBlank()) { "Owner ID is required." }
        require(storeName.isNotEmpty()) { "Store name is required." }
        require(storeCategory.isNotEmpty()) { "Store category is required." }
        require(latitude in -90.0..90.0) { "Invalid store latitude." }
        require(longitude in -180.0..180.0) { "Invalid store longitude." }

        val store = mapOf(
            "ownerId" to ownerId,
            "name" to storeName,
            "category" to storeCategory,
            "location" to GeoPoint(latitude, longitude),
            "address" to "",
            "city" to ""
        )

        val storeDocument = firestore.collection("stores").document(ownerId)

        firestore.runTransaction { transaction ->
            val existingStore = transaction.get(storeDocument)

            if (existingStore.exists()) {
                throw StoreAlreadyExistsException()
            }

            transaction.set(storeDocument, store)
            Unit
        }.await()
    }
}