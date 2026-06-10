package com.erenkadiroglu.saharez.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitClient: Uygulamanın Networking Engine'idir.
 * * Singleton Design Pattern: Uygulama yaşam döngüsü boyunca tek bir HTTP istemcisi (client)
 * nesnesi oluşturur. Bu, bellek yönetimini optimize eder ve ağ kaynaklarının (connection pool)
 * verimli kullanılmasını sağlar.
 */
object RetrofitClient {

    // Sunucu adresi. Retrofit mimarisi gereği URL her zaman '/' karakteri ile sonlanmalıdır.
    private const val BASE_URL = "https://saharez.xyz/saharez/"

    /**
     * Interceptor Pattern:
     * HttpLoggingInterceptor, ağ trafiğini izlemek için kullanılır.
     * Sunucuya giden istekleri ve gelen cevapları (JSON payload) geliştirme aşamasında
     * konsolda (Logcat) izlememizi sağlar. Bu, hata ayıklama (Debugging) sürecini hızlandırır.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Lazy Initialization:
     * Retrofit örneği uygulama başlar başlamaz değil, ilk kez ihtiyaç duyulduğu (örneğin ilk API isteği)
     * anda belleğe alınır. Bu, uygulama açılış hızını (App Launch Performance) artırır.
     */
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // GsonConverter: Gelen JSON verisini otomatik olarak Kotlin Data Class'larına (Serialization/Deserialization) çevirir.
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}