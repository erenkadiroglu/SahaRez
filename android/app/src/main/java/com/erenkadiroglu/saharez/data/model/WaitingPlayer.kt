package com.erenkadiroglu.saharez.data.model

import com.google.gson.annotations.SerializedName

/**
 * WaitingPlayer: Bekleme listesindeki bir oyuncuyu temsil eden DTO (Data Transfer Object).
 * * Teknik Amaç: Sunucudan gelen (PHP backend) JSON verisini, uygulama içerisinde
 * tip güvenli (type-safe) bir nesne yapısına dönüştürmek için kullanılan model sınıfıdır.
 */
data class WaitingPlayer(
    /**
     * @SerializedName: Sunucu tarafındaki JSON anahtar isimleri (örn: "full_name")
     * ile Kotlin değişken isimleri (örn: "name") arasında bir köprü kurar.
     * Bu sayede sunucu tarafındaki isimlendirme standartlarından bağımsız çalışabiliriz
     * (Decoupling) ve kodumuzu daha okunaklı hale getiririz.
     */
    @SerializedName("id")
    val id: Int,

    @SerializedName("full_name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("time_slot")
    val timeSlot: String
)