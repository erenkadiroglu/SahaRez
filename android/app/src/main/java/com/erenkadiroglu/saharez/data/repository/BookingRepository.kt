package com.erenkadiroglu.saharez.data.repository

import com.erenkadiroglu.saharez.data.local.AdDao
import com.erenkadiroglu.saharez.data.local.BookingDao
import com.erenkadiroglu.saharez.data.model.Booking
import com.erenkadiroglu.saharez.data.model.BookingRequest
import com.erenkadiroglu.saharez.data.model.DbBooking
import com.erenkadiroglu.saharez.data.model.MatchAd
import com.erenkadiroglu.saharez.data.model.PlayerPaymentResponse
import com.erenkadiroglu.saharez.data.remote.RetrofitClient
import com.erenkadiroglu.saharez.utils.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * BookingRepository: Veri Sağlayıcı Katman (Data Layer)
 * * Hem Randevuların (Bookings) hem de İlanların (MatchAds) Offline-First yönetimini sağlar.
 * * Veri Köprüsü: Sunucudan (Remote) ve Cihazdan (Local) gelen verileri eşleyerek View'a sunar.
 */
class BookingRepository(
    private val bookingDao: BookingDao,
    private val adDao: AdDao
) {
    private val apiService = RetrofitClient.instance

    // --- RANDEVU (BOOKING) İŞLEMLERİ ---

    fun getDailyBookings(date: String): Flow<NetworkResult<List<DbBooking>>> = flow {
        emit(NetworkResult.Loading())
        try {
            syncOfflineBookings()

            val allLocalData = bookingDao.getAllBookings().first()
            val dailyLocalData = allLocalData.filter { it.matchDate == date }
            emit(NetworkResult.Success(mapToDbBooking(dailyLocalData)))

            val response = apiService.getDailyBookings(date).execute()
            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!
                if (serverData.status == "success" && serverData.data != null) {
                    bookingDao.clearSyncedBookingsByDate(date)
                    serverData.data.forEach { dbBooking ->
                        // HATA ÇÖZÜLDÜ: Sunucudan gelen oyuncuları Booking Entity'sine bağlıyoruz.
                        val newBooking = Booking(
                            remoteId = dbBooking.id,
                            userId = 0,
                            fullName = dbBooking.fullName ?: "",
                            phone = dbBooking.phone ?: "",
                            hasShuttle = dbBooking.hasShuttle,
                            matchDate = date,
                            timeSlot = dbBooking.timeSlot,
                            isSynced = true,
                            waitingCount = dbBooking.waitingCount,
                            waitingUsers = dbBooking.waitingUsers,

                            // Gelen PlayerPaymentResponse modelini yerel PlayerPayment modeline çeviriyoruz
                            players = dbBooking.players.map { pr ->
                                com.erenkadiroglu.saharez.data.model.PlayerPayment(
                                    name = pr.name ?: "Bilinmeyen",
                                    rentedCleats = pr.rentedCleats,
                                    buffetExpense = pr.buffetExpense
                                )
                            }
                        )
                        bookingDao.insertBooking(newBooking)
                    }
                    val updatedLocalData = bookingDao.getAllBookings().first().filter { it.matchDate == date }
                    emit(NetworkResult.Success(mapToDbBooking(updatedLocalData)))
                }
            }
        } catch (e: Exception) {
            val offlineData = bookingDao.getAllBookings().first().filter { it.matchDate == date }
            if (offlineData.isEmpty()) {
                emit(NetworkResult.Error("Bağlantı hatası: Çevrimdışı boş randevu şablonu oluşturuldu."))
                emit(NetworkResult.Success(emptyList()))
            } else {
                emit(NetworkResult.Error("Bağlantı hatası: Çevrimdışı alınan veriler gösteriliyor."))
                emit(NetworkResult.Success(mapToDbBooking(offlineData)))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun createBooking(booking: Booking, request: BookingRequest): Flow<NetworkResult<Booking>> = flow {
        emit(NetworkResult.Loading())
        try {
            val call = apiService.createBooking(request)
            val response = call.execute()

            if (response.isSuccessful && response.body()?.status == "success") {
                booking.isSynced = true
                bookingDao.insertBooking(booking)
                emit(NetworkResult.Success(booking))
            } else {
                emit(NetworkResult.Error(response.body()?.message ?: "Bilinmeyen sunucu hatası"))
            }
        } catch (e: Exception) {
            booking.isSynced = false
            bookingDao.insertBooking(booking)
            emit(NetworkResult.Success(booking))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun syncOfflineBookings() = withContext(Dispatchers.IO) {
        try {
            val unsyncedList = bookingDao.getUnsyncedBookings()
            for (booking in unsyncedList) {
                // HATA ÇÖZÜLDÜ: Değişken isimleri (named parameters) uyumsuzluğu engellenerek
                // değerler doğrudan sırasıyla (positional arguments) aktarıldı.
                val requestPlayers = booking.players.map { p ->
                    com.erenkadiroglu.saharez.data.model.PlayerPaymentRequest(
                        p.name,
                        p.rentedCleats,
                        p.buffetExpense
                    )
                }

                val request = BookingRequest(
                    user_id = booking.userId,
                    full_name = booking.fullName,
                    phone = booking.phone,
                    match_date = booking.matchDate,
                    time_slot = booking.timeSlot,
                    has_shuttle = booking.hasShuttle,
                    players = requestPlayers
                )
                val response = apiService.createBooking(request).execute()
                if (response.isSuccessful) {
                    bookingDao.markAsSynced(booking.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- İLAN (MATCH AD) İŞLEMLERİ ---

    fun getMatchAds(date: String): Flow<NetworkResult<List<MatchAd>>> = flow {
        emit(NetworkResult.Loading())
        try {
            syncOfflineAds()
            val localAds = adDao.getAdsByDate(date).first()
            emit(NetworkResult.Success(localAds))

            val response = apiService.getMatchAds(date).execute()
            if (response.isSuccessful && response.body() != null) {
                val serverData = response.body()!!
                if (serverData.status == "success") {
                    adDao.clearSyncedAdsByDate(date)
                    serverData.data?.forEach { ad ->
                        ad.matchDate = date
                        ad.isSynced = true
                        adDao.insertAd(ad)
                    }
                    val updatedLocalAds = adDao.getAdsByDate(date).first()
                    emit(NetworkResult.Success(updatedLocalAds))
                }
            }
        } catch (e: Exception) {
            val offlineAds = adDao.getAdsByDate(date).first()
            emit(NetworkResult.Error("Bağlantı hatası: Çevrimdışı ilanlar gösteriliyor."))
            emit(NetworkResult.Success(offlineAds))
        }
    }.flowOn(Dispatchers.IO)

    fun createMatchAdOffline(ad: MatchAd): Flow<NetworkResult<MatchAd>> = flow {
        emit(NetworkResult.Loading())
        try {
            val response = apiService.createMatchAd(
                userId = ad.userId,
                matchDate = ad.matchDate,
                timeSlot = ad.timeSlot,
                adType = ad.type,
                missingCount = ad.missingCount,
                missingPositions = ad.missingPositions
            ).execute()

            if (response.isSuccessful && response.body()?.status == "success") {
                ad.isSynced = true
                adDao.insertAd(ad)
                emit(NetworkResult.Success(ad))
            } else {
                emit(NetworkResult.Error("Sunucu reddetti."))
            }
        } catch (e: Exception) {
            ad.isSynced = false
            adDao.insertAd(ad)
            emit(NetworkResult.Success(ad))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun syncOfflineAds() = withContext(Dispatchers.IO) {
        try {
            val unsyncedAds = adDao.getUnsyncedAds()
            for (ad in unsyncedAds) {
                val response = apiService.createMatchAd(
                    userId = ad.userId,
                    matchDate = ad.matchDate,
                    timeSlot = ad.timeSlot,
                    adType = ad.type,
                    missingCount = ad.missingCount,
                    missingPositions = ad.missingPositions
                ).execute()

                if (response.isSuccessful) {
                    adDao.markAsSynced(ad.localId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Data Mapping (Veri Dönüştürme Katmanı)
     * * Yerel veritabanındaki oyuncular artık boş listeye atılmak
     * yerine, başarıyla ekrandaki "DbBooking" nesnesine aktarılıyor.
     */
    private fun mapToDbBooking(localList: List<Booking>): List<DbBooking> {
        return localList.map { local ->
            DbBooking(
                id = if (local.remoteId != 0) local.remoteId else local.id,
                timeSlot = local.timeSlot,
                fullName = local.fullName,
                phone = local.phone,
                hasShuttle = local.hasShuttle,
                waitingCount = local.waitingCount,
                waitingUsers = local.waitingUsers,

                // Oyuncu köprüsünü kurduk. Yerel 'PlayerPayment' modeli
                // ekrandaki 'PlayerPaymentResponse' modeline çevriliyor.
                players = local.players.map { lp ->
                    PlayerPaymentResponse(
                        name = lp.name,
                        rentedCleats = lp.rentedCleats,
                        buffetExpense = lp.buffetExpense
                    )
                }
            )
        }
    }
}