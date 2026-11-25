package edu.ap.dakaniktochniwegsmijten.data.geocoding

import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeocodingRepository(
    private val addressDao: CachedAddressDao
) {
    suspend fun findStoreAddress(latitude: Double, longitude: Double): StoreAddress? {
        val key = CachedAddress.keyForStorePin(latitude, longitude)
        addressDao.findByKey(key)?.let { return it.address }

        return requestMutex.withLock {
            addressDao.findByKey(key)?.let { return@withLock it.address }

            val waitMillis = nextRequestAtMillis - SystemClock.elapsedRealtime()
            if (waitMillis > 0) {
                delay(waitMillis)
            }

            val response = try {
                api.reverseStoreLocation(latitude, longitude)
            } finally {
                nextRequestAtMillis = SystemClock.elapsedRealtime() + 1_000L
            }

            val address = response.address?.toStoreAddress()
                ?: return@withLock null

            if (address.streetAndNumber.isBlank() &&
                address.postalCode.isBlank() &&
                address.city.isBlank()
            ) {
                return@withLock null
            }

            addressDao.save(
                CachedAddress(
                    key = key,
                    displayName = response.displayName.orEmpty(),
                    address = address,
                    fetchedAt = System.currentTimeMillis()
                )
            )

            address
        }
    }

    companion object {
        private val requestMutex = Mutex()
        private var nextRequestAtMillis = 0L

        private val api = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(
                OkHttpClient.Builder()
                    .retryOnConnectionFailure(false)
                    .followRedirects(false)
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }
}