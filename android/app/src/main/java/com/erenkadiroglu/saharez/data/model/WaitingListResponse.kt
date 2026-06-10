package com.erenkadiroglu.saharez.data.model

/**
 * WaitingListResponse: Bekleme listesi servisi (get_waiting_list.php) için dönen
 * API yanıt yapısını temsil eden Wrapper (Kapsayıcı) DTO sınıfıdır.
 * * * Teknik Amaç: Sunucudan gelen JSON paketini, uygulama içinde tip güvenli (type-safe)
 * bir nesneye dönüştürerek, verinin işlenmesini sağlar.
 */
data class WaitingListResponse(
    /**
     * status: API isteğinin sonucunu belirtir (Örn: "success" veya "error").
     */
    val status: String,

    /**
     * message: Hata yönetimi (Error Handling) için kritik bir alan.
     * Sunucu tarafındaki PHP script'inin (hata durumunda) neden başarısız olduğunu
     * anlamamızı sağlar.
     */
    val message: String?,

    /**
     * data: Liste boş gelirse veya bir hata oluşursa uygulamanın çökmemesi için
     * nullable (?) olarak tanımlanmıştır. "Defensive Programming" (Savunmacı Programlama)
     * gereği veri yoksa uygulamanın boş durumu (Empty State) yönetebilmesi için şarttır.
     */
    val data: List<WaitingPlayer>?
)