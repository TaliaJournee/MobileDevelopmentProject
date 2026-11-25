package edu.ap.dakaniktochniwegsmijten.data.geocoding

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CachedAddressDao {
    @Query("SELECT * FROM cached_addresses WHERE `key` = :key")
    suspend fun findByKey(key: String): CachedAddress?

    @Upsert
    suspend fun save(address: CachedAddress)
}