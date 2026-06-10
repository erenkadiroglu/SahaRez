package com.erenkadiroglu.saharez.data.remote

import com.erenkadiroglu.saharez.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * ApiService: Uygulamanın API Tanımlama Katmanı (API Definition Layer).
 * * Retrofit kütüphanesi kullanılarak, sunucu ile uygulama arasındaki HTTP iletişim protokolleri
 * bu arayüz (interface) üzerinde tanımlanmıştır.
 * * RESTful mimari prensiplerine göre CRUD operasyonları (Create, Read, Update, Delete)
 * uygun HTTP metotları (GET, POST) ile eşleştirilmiştir.
 */
interface ApiService {

    @GET("bookings")
    suspend fun getBookings(): retrofit2.Response<List<Booking>>

    @POST("create_booking.php")
    fun createBooking(@Body request: BookingRequest): Call<BookingResponse>

    @FormUrlEncoded
    @POST("join_waiting_list.php")
    fun joinWaitingList(
        @Field("user_id") userId: Int,
        @Field("match_date") matchDate: String,
        @Field("time_slot") timeSlot: String
    ): Call<BookingResponse>

    @POST("login.php")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @GET("get_waiting_list.php")
    fun getWaitingList(): Call<WaitingListResponse>

    @FormUrlEncoded
    @POST("delete_waiting_player.php")
    fun deleteWaitingPlayer(@Field("id") id: Int): Call<BookingResponse>

    @FormUrlEncoded
    @POST("get_stats.php")
    fun getStats(@Field("date") date: String? = null): Call<StatsResponse>

    @FormUrlEncoded
    @POST("get_daily_bookings.php")
    fun getDailyBookings(@Field("date") date: String): Call<DailyBookingResponse>

    // YENİ EKLENDİ: Randevuyu ve bağlı oyuncuları veritabanından kalıcı olarak siler
    @FormUrlEncoded
    @POST("delete_booking.php")
    fun deleteBooking(
        @Field("match_date") matchDate: String,
        @Field("time_slot") timeSlot: String
    ): Call<BookingResponse>

    @FormUrlEncoded
    @POST("create_match_ad.php")
    fun createMatchAd(
        @Field("user_id") userId: Int,
        @Field("match_date") matchDate: String,
        @Field("time_slot") timeSlot: String,
        @Field("ad_type") adType: String,
        @Field("missing_count") missingCount: String?,
        @Field("missing_positions") missingPositions: String?
    ): Call<BookingResponse>

    @FormUrlEncoded
    @POST("get_match_ads.php")
    fun getMatchAds(@Field("date") date: String): Call<MatchAdResponse>

    @FormUrlEncoded
    @POST("update_match_ad.php")
    fun updateMatchAd(
        @Field("ad_id") adId: String,
        @Field("ad_type") adType: String,
        @Field("missing_count") missingCount: String?,
        @Field("missing_positions") missingPositions: String?
    ): Call<BookingResponse>
}