package com.erenkadiroglu.saharez.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erenkadiroglu.saharez.data.model.Booking
import kotlinx.coroutines.flow.Flow

/**
 * BookingDao (Data Access Object): Veritabanı Erişim Nesnesi.
 */
@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings")
    fun getAllBookings(): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE id = :id")
    fun getBookingById(id: Int): Flow<Booking>

    // --- OFFLINE SENKRONİZASYON MOTORU (Sync Logic) ---

    @Query("SELECT * FROM bookings WHERE isSynced = 0")
    suspend fun getUnsyncedBookings(): List<Booking>

    @Query("UPDATE bookings SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    /**
     * Dublication (Çoğaltma) Önleyici Katman:
     * Ağdan güncel veri geldiğinde, o güne ait daha önce başarıyla senkronize edilmiş
     * eski verileri temizler. Ancak internet yokken alınmış (isSynced = 0) kayıtlara DOKUNMAZ.
     */
    @Query("DELETE FROM bookings WHERE matchDate = :date AND isSynced = 1")
    suspend fun clearSyncedBookingsByDate(date: String)
}