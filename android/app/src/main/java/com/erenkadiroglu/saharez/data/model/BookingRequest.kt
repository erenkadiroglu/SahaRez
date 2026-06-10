package com.erenkadiroglu.saharez.data.model

/**
 * BookingRequest: API'ye randevu oluşturma isteği (POST) gönderirken kullanılan
 * bir DTO (Data Transfer Object - Veri Transfer Nesnesi) sınıfıdır.
 * * Amaç: Yerel veritabanı şeman (Room Entity) ile API'nin beklediği JSON yapısını
 * birbirinden ayırarak (Decoupling) sistemin esnekliğini artırmaktır.
 */
data class BookingRequest(
    val user_id: Int,
    val full_name: String,
    val phone: String,
    val match_date: String,
    val time_slot: String,
    val has_shuttle: Int,
    // İç içe geçmiş (nested) nesne yapısı: API, tek bir istekte hem rezervasyon
    // bilgilerini hem de oyuncu detaylarını tek seferde kabul eder.
    val players: List<PlayerPaymentRequest>
)

/**
 * PlayerPaymentRequest: Randevu detayındaki alt oyuncu bilgilerini temsil eden DTO sınıfı.
 * Sunucu tarafında her bir oyuncunun maliyet hesabını yapabilmesi için ayrıştırılmış veridir.
 */
data class PlayerPaymentRequest(
    val name: String,
    val rented_cleats: Boolean, // Krampon kiralama durumu
    val buffet_expense: Int     // Büfe harcaması
)