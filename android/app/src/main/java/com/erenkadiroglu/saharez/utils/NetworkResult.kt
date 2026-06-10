package com.erenkadiroglu.saharez.utils

/**
 * NetworkResult: Durum Yönetimi (State Management) için kullanılan "Result Wrapper" deseni.
 * * Teknik Detaylar:
 * 1. Sealed Class (Mühürlü Sınıf): Hiyerarşiyi kısıtlar. Bu, derleyicinin (compiler)
 * tüm olası durumları (Success, Error, Loading) bildiği anlamına gelir.
 * Bu sayede "exhaustive check" (tam kapsamlı kontrol) yaparak beklenmedik durumların önüne geçilir.
 * 2. Generic (T): Sınıfın farklı veri tipleriyle (Booking, List<Stats>, vb.) tekrar kullanılabilir
 * olmasını (Reusability) sağlar.
 * 3. Separation of Concerns: Verinin ağdan çekilme durumu ile verinin kendisini aynı paket içinde
 * taşıyarak, Activity katmanına "işin durumu ne?" sorusuna tek bir cevap ile dönülmesini sağlar.
 */
sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * İşlem başarıyla tamamlandığında veriyi taşıyan durum.
     */
    class Success<T>(data: T) : NetworkResult<T>(data)

    /**
     * İşlem başarısız olduğunda hata mesajını ve opsiyonel olarak mevcut veriyi taşıyan durum.
     */
    class Error<T>(message: String, data: T? = null) : NetworkResult<T>(data, message)

    /**
     * İşlemin devam ettiği (Loading) durum. UI katmanında Progress Bar vb. göstermek için kullanılır.
     */
    class Loading<T> : NetworkResult<T>()
}