package edu.ap.dakaniktochniwegsmijten.data.geocoding

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface NominatimApi {
    @Headers("User-Agent: DaKanIkTochNiWegsmijten/1.0 (edu.ap.dakaniktochniwegsmijten)")
    @GET("reverse?format=json&zoom=18&addressdetails=1")
    suspend fun reverseStoreLocation(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double
    ): NominatimResponse
}

data class NominatimResponse(
    @SerializedName("display_name") val displayName: String?,
    val address: NominatimAddress?
)

data class NominatimAddress(
    val road: String?,
    @SerializedName("house_number") val houseNumber: String?,
    val postcode: String?,
    val city: String?,
    val town: String?,
    val village: String?
)

data class StoreAddress(
    val streetAndNumber: String,
    val postalCode: String,
    val city: String
)

fun NominatimAddress.toStoreAddress(): StoreAddress {
    val streetAndNumber = listOfNotNull(road, houseNumber)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .joinToString(" ")

    val municipality = listOfNotNull(city, town, village)
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }

    return StoreAddress(
        streetAndNumber = streetAndNumber,
        postalCode = postcode.orEmpty().trim(),
        city = municipality.orEmpty()
    )
}