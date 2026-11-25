package edu.ap.dakaniktochniwegsmijten.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import edu.ap.dakaniktochniwegsmijten.data.geocoding.CachedAddress
import edu.ap.dakaniktochniwegsmijten.data.geocoding.CachedAddressDao

@Database(
    entities = [CachedAddress::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedAddressDao(): CachedAddressDao

    companion object {
        private var instance: AppDatabase? = null

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app.db"
            ).build().also { instance = it }
        }
    }
}