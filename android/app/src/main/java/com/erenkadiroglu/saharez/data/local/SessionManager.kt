package com.erenkadiroglu.saharez.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * SessionManager: Uygulama İçi Oturum ve Durum Yönetimi (State Management).
 * * Persistent (Kalıcı) ve In-Memory (Geçici) olmak üzere iki farklı oturum tipi destekler.
 * * Güvenlik: Veriler MODE_PRIVATE ile izole edilir.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("SahaRezPrefs", Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    /**
     * saveLoginSession: "Beni Hatırla" seçimine göre oturumun nerede saklanacağına karar verir.
     * @param rememberMe true ise SharedPreferences'a (Kalıcı), false ise Memory'e (Geçici) yazar.
     */
    fun saveLoginSession(isLoggedIn: Boolean, userRole: String, userId: Int, userName: String, rememberMe: Boolean) {
        if (rememberMe) {
            // Kalıcı Oturum (Uygulama kapatılsa bile hatırlanır)
            editor.putBoolean("isLoggedIn", isLoggedIn)
            editor.putString("USER_ROLE", userRole)
            editor.putInt("USER_ID_INT", userId)
            editor.putString("USER_NAME", userName)
            editor.apply()

            // Geçici hafızayı temizle (Çakışmayı önlemek için)
            InMemorySession.clear()
        } else {
            // Geçici Oturum (Sadece uygulama açıkken RAM'de yaşar)
            InMemorySession.isLoggedIn = isLoggedIn
            InMemorySession.userRole = userRole
            InMemorySession.userId = userId
            InMemorySession.userName = userName

            // Kalıcı hafızayı temizle
            editor.clear()
            editor.apply()
        }
    }

    // Getter Metotları: Önce geçici hafızaya, yoksa kalıcı hafızaya bakar.
    fun isLoggedIn(): Boolean = InMemorySession.isLoggedIn ?: prefs.getBoolean("isLoggedIn", false)

    fun getUserRole(): String = InMemorySession.userRole ?: prefs.getString("USER_ROLE", "PLAYER") ?: "PLAYER"

    fun getUserId(): Int = InMemorySession.userId ?: prefs.getInt("USER_ID_INT", 0)

    fun getUserName(): String = InMemorySession.userName ?: prefs.getString("USER_NAME", "Bilinmeyen") ?: "Bilinmeyen"

    fun logout() {
        // Hem kalıcı hem de geçici verileri yok eder (Security Invalidation)
        editor.clear()
        editor.apply()
        InMemorySession.clear()
    }

    /**
     * RAM üzerinde yaşayan Singleton nesne (Geçici Oturum)
     */
    private object InMemorySession {
        var isLoggedIn: Boolean? = null
        var userRole: String? = null
        var userId: Int? = null
        var userName: String? = null

        fun clear() {
            isLoggedIn = null
            userRole = null
            userId = null
            userName = null
        }
    }
}