package com.erenkadiroglu.saharez.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erenkadiroglu.saharez.data.model.MatchAd
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    // Sadece seçili güne ait ilanları getirir (Reaktif Flow mimarisi)
    @Query("SELECT * FROM match_ads WHERE matchDate = :date")
    fun getAdsByDate(date: String): Flow<List<MatchAd>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: MatchAd)

    // İnternet yokken oluşturulan (isSynced = 0) ilanları kuyruğa alır
    @Query("SELECT * FROM match_ads WHERE isSynced = 0")
    suspend fun getUnsyncedAds(): List<MatchAd>

    // Sunucuya başarıyla giden ilanı işaretler
    @Query("UPDATE match_ads SET isSynced = 1 WHERE localId = :localId")
    suspend fun markAsSynced(localId: Int)

    // Ağdan taze veri geldiğinde o güne ait eski başarılı kayıtları silerek dublication önler
    @Query("DELETE FROM match_ads WHERE matchDate = :date AND isSynced = 1")
    suspend fun clearSyncedAdsByDate(date: String)
}