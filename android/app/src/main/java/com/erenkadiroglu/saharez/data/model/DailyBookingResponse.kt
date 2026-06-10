package com.erenkadiroglu.saharez.data.model

import com.google.gson.annotations.SerializedName

/**
 * DailyBookingResponse: Sunucu tarafındaki API endpoint'inin (get_daily_bookings.php)
 * yanıt yapısını temsil eden kapsayıcı (Wrapper) DTO sınıfıdır.
 * * Teknik Amaç: Retrofit/Gson kullanarak sunucudan gelen JSON formatındaki verinin
 * tip güvenli bir şekilde Kotlin nesnesine "Deserialization" işlemine tabi tutulması.
 */
data class DailyBookingResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: List<DbBooking>?
)

/**
 * DbBooking: Uygulamanın temel veri modelidir.
 * * @SerializedName: Sunucudan gelen JSON anahtarı ile Kotlin değişken ismi arasında
 * bir eşleme (Mapping) sağlar. Bu sayede sunucu tarafındaki isimlendirme (örneğin snake_case)
 * mobil uygulamadaki kod standartlarını bozmaz.
 */
data class DbBooking(
    @SerializedName("id") val id: Int,
    @SerializedName("timeSlot") val timeSlot: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("phone") val phone: String?,

    /**
     * Data Contract Mapping:
     * Sunucudan "isDepositPaid" ismiyle gelen veriyi, uygulama içi mantığı (business logic)
     * gereği "hasShuttle" değişkenine eşleyerek, API sözleşmesini uygulama mimarisiyle
     * uyumlu hale getirdik (Contract Decoupling).
     */
    @SerializedName("isDepositPaid") val hasShuttle: Int,

    /**
     * Business Logic Expansion:
     * İhtiyaca yönelik yeni eklenen bekleme listesi metrikleri.
     * Nullable (null gelebilen) alanlar yerine varsayılan değerler (Default Values)
     * verilerek uygulamanın çalışma zamanı hatalarına (Runtime Exception) karşı
     * "Defensive Programming" (Savunmacı Programlama) yapısı güçlendirilmiştir.
     */
    @SerializedName("waitingCount") val waitingCount: Int = 0,
    @SerializedName("waitingUsers") val waitingUsers: List<Int> = emptyList(),

    // Defansif Programlama: NullPointerException (NPE) hatalarını engellemek ve
    // liste silinmelerinin önüne geçmek için varsayılan olarak boş liste (emptyList()) atanmıştır.
    @SerializedName("players") val players: List<PlayerPaymentResponse> = emptyList()
)

/**
 * PlayerPaymentResponse: Randevu içerisindeki oyuncu detaylarını temsil eden alt DTO yapısı.
 * * İlişkisel Veri Yönetimi: Randevu tablosu ile oyuncu ödeme detayları arasındaki
 * iç içe geçmiş (nested) ilişkiyi modellemek için kullanılır.
 */
data class PlayerPaymentResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("rentedCleats") val rentedCleats: Boolean,
    @SerializedName("buffetExpense") val buffetExpense: Int
)