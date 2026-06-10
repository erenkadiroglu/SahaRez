package com.erenkadiroglu.saharez.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.erenkadiroglu.saharez.data.model.Booking
import com.erenkadiroglu.saharez.data.model.MatchAd

@Database(entities = [Booking::class, MatchAd::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookingDao(): BookingDao
    abstract fun adDao(): AdDao // İlanlar için YENİ veritabanı erişim noktası

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saharez_database"
                )
                    .fallbackToDestructiveMigration() // Çökmeleri önleyen şema yenileyici
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}