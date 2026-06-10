package com.erenkadiroglu.saharez.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class MatchAdResponse(
    val status: String,
    val message: String?,
    val data: List<MatchAd>?
)

/**
 * MatchAd: İlan (Advertisement) Veri Modeli ve Room Tablosu.
 * Hem Retrofit'ten gelen JSON verisini tutar hem de cihazın yerel veritabanında saklanır.
 */
@Entity(tableName = "match_ads")
data class MatchAd(
    @PrimaryKey(autoGenerate = true)
    var localId: Int = 0, // Cihaz içindeki benzersiz ID (Offline oluşturulan ilanlar için şarttır)

    @SerializedName("id")
    var id: String = "", // Sunucudaki gerçek ID

    var userId: Int = 0, // Çevrimdışı senkronizasyon için ilanı kimin açtığını tutar

    var matchDate: String = "", // O güne ait ilanları filtreleyebilmek için eklenen tarih bayrağı

    @SerializedName("timeSlot") var timeSlot: String = "",
    @SerializedName("creatorName") var creatorName: String = "",
    @SerializedName("creatorPhone") var creatorPhone: String = "",
    @SerializedName("type") var type: String = "",
    @SerializedName("missingCount") var missingCount: String? = null,
    @SerializedName("missingPositions") var missingPositions: String? = null,

    var isSynced: Boolean = true // Çevrimdışı senkronizasyon motoru için kontrol bayrağı
)