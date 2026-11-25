package edu.ap.dakaniktochniwegsmijten.data.store

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import edu.ap.dakaniktochniwegsmijten.data.geocoding.StoreAddress

data class MerchantStore(
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: StoreAddress
)
class StoreAlreadyExistsException : Exception("The merchant already has a store.")

class StoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getStore(ownerId: String): MerchantStore? {
        require(ownerId.isNotBlank()) { "Owner ID is required." }

        val document = firestore.collection("stores")
            .document(ownerId)
            .get()
            .await()

        if (!document.exists()) return null

        val location = checkNotNull(document.getGeoPoint("location")) {
            "The stored location is missing."
        }

        return MerchantStore(
            name = checkNotNull(document.getString("name")) {
                "The store name is missing."
            },
            category = checkNotNull(document.getString("category")) {
                "The store category is missing."
            },
            latitude = location.latitude,
            longitude = location.longitude,
            address = StoreAddress(
                streetAndNumber = document.getString("address").orEmpty(),
                postalCode = document.getString("postalCode").orEmpty(),
                city = document.getString("city").orEmpty()
            )
        )
    }

    suspend fun createStore(
        ownerId: String,
        name: String,
        category: String,
        latitude: Double,
        longitude: Double,
        address: StoreAddress
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
            "address" to address.streetAndNumber.trim(),
            "postalCode" to address.postalCode.trim(),
            "city" to address.city.trim()
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