package com.erenkadiroglu.saharez.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/**
 * Converters: Room Veritabanı Tip Dönüştürücü.
 * * SQLite karmaşık listeleri (List<Int>, List<PlayerPayment>) doğrudan saklayamaz.
 * Gson kullanarak bu karmaşık nesneleri JSON string formatına dönüştürüyoruz.
 * * Null-Safety: Veritabanından null dönme ihtimaline karşı güvenliğe alınmıştır.
 */
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<Int>?): String {
        return Gson().toJson(list ?: emptyList<Int>())
    }

    // YENİ EKLENDİ: Oyuncu listesini yerel veritabanına kaydetmek için dönüştürücüler
    @TypeConverter
    fun fromPlayerListString(value: String?): List<PlayerPayment> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<PlayerPayment>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun toPlayerListString(list: List<PlayerPayment>?): String {
        return Gson().toJson(list ?: emptyList<PlayerPayment>())
    }
}

/**
 * Booking (Entity): Yerel Veritabanı Şeması.
 * * Hem sunucu verisini tutar hem de çevrimdışı (Offline-First) senaryolarda
 * uygulamanın hafıza merkezi olarak görev yapar.
 */
@Entity(tableName = "bookings")
@TypeConverters(Converters::class)
data class Booking(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @SerializedName("id") var remoteId: Int = 0,
    @SerializedName("user_id") var userId: Int = 0,
    @SerializedName("full_name") var fullName: String = "",
    @SerializedName("phone") var phone: String = "",
    @SerializedName("has_shuttle") var hasShuttle: Int = 0,
    @SerializedName("match_date") var matchDate: String = "",
    @SerializedName("time_slot") var timeSlot: String = "",
    @SerializedName("status") var status: String = "PENDING",
    var isSynced: Boolean = false,

    @SerializedName("waitingCount") var waitingCount: Int = 0,
    @SerializedName("waitingUsers") var waitingUsers: List<Int> = emptyList(),

    // KRİTİK GÜNCELLEME: Uygulama kapanıp açıldığında "Maç Sonrası Ödeme Yapanlar"
    // listesinin silinmemesi için yerel tabloya (Room) oyuncular sütunu eklendi.
    @SerializedName("players") var players: List<PlayerPayment> = emptyList()
)

/**
 * BookingResponse: API Yanıt Modeli (DTO - Data Transfer Object).
 */
data class BookingResponse(
    val status: String,
    val message: String
)