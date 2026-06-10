package com.erenkadiroglu.saharez.data.model

/**
 * PlayerPayment: Bir rezervasyona katılan oyuncunun detaylı ödeme ve ekstra hizmet
 * bilgilerini temsil eden Domain Modeli.
 * * Teknik Rolü: Uygulama içerisinde, randevu toplam tutarının hesaplanması (calculation engine)
 * ve her bir oyuncunun krampon/büfe gibi ekstra harcamalarının takip edilmesi için kullanılan
 * bir "Business Domain" nesnesidir.
 */
data class PlayerPayment(
    val name: String,

    // Krampon kiralama durumu: Boolean bayrak ile ekstra hizmet takibi.
    val rentedCleats: Boolean,

    // Büfe harcaması: Sayısal değer (Integer) ile maliyet hesaplaması.
    val buffetExpense: Int
)