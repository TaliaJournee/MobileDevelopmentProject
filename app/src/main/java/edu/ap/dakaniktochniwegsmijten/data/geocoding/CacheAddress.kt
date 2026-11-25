package edu.ap.dakaniktochniwegsmijten.data.geocoding

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

@Entity(tableName = "cached_addresses")
data class CachedAddress(
    @PrimaryKey val key: String,
    val displayName: String,
    @Embedded val address: StoreAddress,
    val fetchedAt: Long
) {
    companion object {
        fun keyForStorePin(latitude: Double, longitude: Double): String {
            val latitudeE4 = (latitude * 10_000).roundToInt()
            val longitudeE4 = (longitude * 10_000).roundToInt()
            return "18:$latitudeE4:$longitudeE4"
        }
    }
}