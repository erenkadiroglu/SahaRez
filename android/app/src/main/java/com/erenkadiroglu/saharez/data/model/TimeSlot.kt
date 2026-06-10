package com.erenkadiroglu.saharez.data.model

/**
 * TimeSlot: Randevu seanslarını temsil eden "UI-Ready" veri modeli.
 * * Teknik Rolü: API'den veya veritabanından gelen veriyi, uygulamanın arayüz (UI) katmanında
 * doğrudan görüntülenebilecek şekilde zenginleştirir.
 * * State Management: Sadece veriyi taşımakla kalmaz; seansın doluluk durumu (isAvailable)
 * veya bekleme listesi gibi "Runtime State" bilgilerini de tutarak arayüzün dinamik
 * güncellenmesini sağlar.
 */
data class TimeSlot(
    val time: String,
    val phone: String, // İlanlar için saha iletişim numarası

    // UI State: Seansın o anki durumunu temsil eden bayraklar
    var isAvailable: Boolean = true,
    var fullName: String? = null,
    var phoneNo: String? = null,

    // İş Mantığı (Business Logic): Depozito yerine servis (shuttle) bilgisi
    // ile modelin güncellenmiş hali.
    var hasShuttle: Boolean = false,

    // Detaylı Bilgiler: Randevu içindeki oyuncu ödemeleri ve saha fiyatı.
    var paidPlayers: MutableList<PlayerPayment> = mutableListOf(),
    val courtPrice: Int = 1500,

    // Dinamik Veri: Bekleme listesi için API'den gelen anlık kullanıcı listesi.
    var waitingCount: Int = 0,
    var waitingUsers: List<Int> = emptyList()
)