package com.erenkadiroglu.saharez.data.model

import com.google.gson.annotations.SerializedName

/**
 * StatsResponse: İstatistik paneli (Dashboard) API yanıt yapısı.
 * * Teknik Amaç: Sunucudan gelen karmaşık JSON verisini, uygulama içerisinde
 * görselleştirilebilir "tip güvenli" (type-safe) bir nesne yapısına dönüştürmek için
 * kullanılan Wrapper (Kapsayıcı) DTO sınıfıdır.
 */
data class StatsResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: StatsData?
)

/**
 * StatsData: Saha işletme metriklerini içeren veri modeli.
 * * Teknik Rolü: Saha rezervasyon sisteminin finansal ve operasyonel verilerini (KPI) tutar.
 * * İş Mantığı (Business Logic):
 * - Double tipleri: Hassas finansal hesaplamalar (gelir, büfe harcaması vb.) için kullanılmıştır.
 * - OccupancyRate: Sistemde henüz entegrasyonu tamamlanmadığı için varsayılan olarak -1 döner;
 * bu, uygulamanın "veri yok" durumunu anlamasını sağlayan bir "Sentinel Value" (Nöbetçi Değer) kullanımıdır.
 */
data class StatsData(
    @SerializedName("total_bookings") val totalBookings: Int,
    @SerializedName("total_base") val totalBase: Double,
    @SerializedName("total_cleats") val totalCleats: Double,
    @SerializedName("total_buffet") val totalBuffet: Double,
    @SerializedName("grand_total") val grandTotal: Double,

    @SerializedName("avg_revenue") val avgRevenue: Double,
    @SerializedName("cleat_count") val cleatCount: Int,
    @SerializedName("avg_buffet") val avgBuffet: Double,

    // SideIncomeRatio: Yan gelir oranını ifade eden bir metrik (Tam sayı ile temsil edilir).
    @SerializedName("side_income_ratio") val sideIncomeRatio: Int,

    @SerializedName("popular_time") val popularTime: String,

    // CancelRatio: Rezervasyon iptal oranlarını izlemek için kullanılan bir metrik.
    @SerializedName("cancel_ratio") val cancelRatio: Int,

    // OccupancyRate: Saha doluluk oranı. -1 değeri, verinin henüz hesaplanamadığını/mevcut olmadığını belirtir.
    @SerializedName("occupancy_rate") val occupancyRate: Int
)