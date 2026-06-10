package com.erenkadiroglu.saharez.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erenkadiroglu.saharez.data.model.Booking
import com.erenkadiroglu.saharez.data.model.BookingRequest
import com.erenkadiroglu.saharez.data.model.DbBooking
import com.erenkadiroglu.saharez.data.model.MatchAd
import com.erenkadiroglu.saharez.data.repository.BookingRepository
import com.erenkadiroglu.saharez.utils.NetworkResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch

/**
 * BookingViewModel: MVVM Mimarisi State (Durum) Yöneticisi
 * * Separation of Concerns (Sorumlulukların Ayrılması): Kullanıcı arayüzü (Activity) ile
 * veri erişim katmanı (Repository) arasındaki doğrudan bağımlılığı ortadan kaldırır.
 * * Lifecycle-Aware (Yaşam Döngüsü Duyarlılığı): Cihaz ekranı döndürüldüğünde veya
 * konfigürasyon değiştiğinde verilerin kaybolmamasını (State Retention) sağlar.
 */
class BookingViewModel(private val repository: BookingRepository) : ViewModel() {

    // --- ENCAPSULATION (Kapsülleme) ---
    // Verilerin dışarıdan doğrudan değiştirilmesini engellemek için sadece okunabilir (LiveData)
    // versiyonları View katmanına açılmıştır. Değişiklikler sadece ViewModel içinden yapılabilir.

    // Randevu Akışları (Streams)
    private val _dailyBookingsResponse = MutableLiveData<NetworkResult<List<DbBooking>>>()
    val dailyBookingsResponse: LiveData<NetworkResult<List<DbBooking>>> get() = _dailyBookingsResponse

    private val _createBookingResponse = MutableLiveData<NetworkResult<Booking>>()
    val createBookingResponse: LiveData<NetworkResult<Booking>> get() = _createBookingResponse

    // İlan (MatchAd) Akışları
    private val _matchAdsResponse = MutableLiveData<NetworkResult<List<MatchAd>>>()
    val matchAdsResponse: LiveData<NetworkResult<List<MatchAd>>> get() = _matchAdsResponse

    private val _createAdResponse = MutableLiveData<NetworkResult<MatchAd>>()
    val createAdResponse: LiveData<NetworkResult<MatchAd>> get() = _createAdResponse

    /**
     * Crash Prevention Layer (Çökme Önleyici Katman):
     * Coroutine'ler içinde meydana gelebilecek asenkron ağ veya veritabanı istisnalarını
     * (Exception) yakalayarak uygulamanın çökmesini engeller ve UI'a kontrollü hata fırlatır.
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        _dailyBookingsResponse.postValue(NetworkResult.Error("Sistem Hatası: ${exception.message}"))
    }

    // --- ASENKRON İŞ KONTROLÜ (Coroutine Scope) ---

    fun fetchDailyBookings(date: String) {
        viewModelScope.launch(exceptionHandler) {
            // Reaktif veri akışı (Flow) dinlenerek UI anlık olarak beslenir.
            repository.getDailyBookings(date).collect { result ->
                _dailyBookingsResponse.value = result
            }
        }
    }

    fun createNewBooking(booking: Booking, request: BookingRequest) {
        viewModelScope.launch(exceptionHandler) {
            repository.createBooking(booking, request).collect { result ->
                _createBookingResponse.value = result
            }
        }
    }

    fun fetchMatchAds(date: String) {
        viewModelScope.launch(exceptionHandler) {
            repository.getMatchAds(date).collect { result ->
                _matchAdsResponse.value = result
            }
        }
    }

    fun createNewMatchAd(ad: MatchAd) {
        viewModelScope.launch(exceptionHandler) {
            repository.createMatchAdOffline(ad).collect { result ->
                _createAdResponse.value = result
            }
        }
    }

    /**
     * Auto-Sync (Otomatik Senkronizasyon) Tetikleyicisi:
     * Cihaz çevrimiçi olduğunda bekleyen tüm yerel verileri sunucuya aktarır.
     */
    fun syncOfflineData() {
        viewModelScope.launch {
            repository.syncOfflineBookings()
            repository.syncOfflineAds()
        }
    }
}