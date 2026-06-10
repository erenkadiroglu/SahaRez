package com.erenkadiroglu.saharez.data.model

import com.google.gson.annotations.SerializedName

/**
 * LoginResponse: Sunucudan dönen giriş yanıtını karşılayan DTO (Data Transfer Object).
 * * Teknik Amaç: Sunucu tarafındaki JSON yanıtını uygulama içi modeline (Kotlin nesnesi)
 * "Deserialization" işlemi ile güvenli bir şekilde aktarmak.
 * * Data Contract: Bu sınıf, API ile uygulama arasındaki veri transfer sözleşmesini temsil eder.
 */
data class LoginResponse(
    // status ve message alanları her zaman döneceği için non-nullable (zorunlu) tanımlanmıştır.
    val status: String,
    val message: String,

    /**
     * @SerializedName: Sunucu tarafındaki JSON anahtar isimleri (örn: full_name) ile
     * Kotlin değişken isimleri (full_name) arasında bir köprü kurar.
     * Bu sayede sunucu tarafındaki isimlendirme standartlarından bağımsız çalışabiliriz (Decoupling).
     */
    @SerializedName("full_name") val full_name: String? = null,
    @SerializedName("user_id") val userId: Int? = null,

    /**
     * Role (Authorization): Kullanıcının sistemdeki yetki seviyesi.
     * Nullable (null gelebilir) olarak tanımlanmıştır çünkü giriş başarısız olursa
     * sunucu bu alanları göndermeyebilir. "Defensive Programming" gereği
     * bu tip alanları nullable ve default değerle tanımlamak crash riskini minimize eder.
     */
    @SerializedName("role") val role: String? = null
)