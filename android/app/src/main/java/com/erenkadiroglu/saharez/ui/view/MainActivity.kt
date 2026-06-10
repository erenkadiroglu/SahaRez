package com.erenkadiroglu.saharez.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.erenkadiroglu.saharez.databinding.ActivityMainBinding
import com.erenkadiroglu.saharez.data.local.SessionManager
import com.erenkadiroglu.saharez.utils.showConfirmationDialog

/**
 * MainActivity: Uygulamanın Merkezi Navigasyon (Yönlendirme) Paneli.
 * * Bu sınıf, uygulamadaki "Role-Based Access Control" (RBAC - Rol Tabanlı Yetkilendirme)
 * mimarisinin UI katmanındaki en önemli uygulayıcısıdır. Kullanıcının oturum bilgilerini
 * kontrol ederek arayüzün hangi kısımlarının erişilebilir olduğunu dinamik olarak yönetir.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding: XML layout dosyasına tip güvenli (type-safe) erişim sağlar.
    private lateinit var binding: ActivityMainBinding
    // SessionManager: Oturum yönetimi ve yerel veri saklama (SharedPreferences) işlemleri için yardımcı sınıf.
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // UI bileşenlerinin başlatılması
        setupUI()
        setupClickListeners()
        setupBackPress()
    }

    /**
     * Yetkilendirme Kontrolü (RBAC):
     * Oturum açan kullanıcının rolü (ADMIN/PLAYER) kontrol edilir.
     * Sadece yetkili olan (ADMIN) rollerin istatistik ve bekleme listesi gibi
     * yönetimsel panellere erişimi sağlanır. Diğer kullanıcılar için bu butonlar
     * tamamen gizlenerek arayüz karmaşası (UI Clutter) önlenir.
     */
    private fun setupUI() {
        val role = sessionManager.getUserRole()
        val isAdmin = role == "ADMIN"

        binding.textRole.text = if (isAdmin) "Rol: Saha Görevlisi" else "Rol: Oyuncu"

        // Görünürlük mantığı (Conditional Visibility)
        binding.btnGoToStatistics.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.btnGoToWaitingList.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    /**
     * Olay Yönetimi:
     * Uygulama içi yönlendirmeleri ve oturum sonlandırma (Logout) işlemlerini yönetir.
     */
    private fun setupClickListeners() {
        // Randevu Ekranına Geçiş
        binding.btnGoToBookings.setOnClickListener {
            startActivity(Intent(this, BookingActivity::class.java))
        }

        // İstatistik Ekranına Geçiş (Sadece Admin)
        binding.btnGoToStatistics.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        // Bekleme Listesi Ekranına Geçiş (Sadece Admin)
        binding.btnGoToWaitingList.setOnClickListener {
            startActivity(Intent(this, WaitingListActivity::class.java))
        }

        // Oturumu Güvenli Sonlandırma
        binding.btnLogout.setOnClickListener {
            showConfirmationDialog(
                title = "Çıkış Yap",
                message = "Hesabınızdan çıkış yapmak istediğinize emin misiniz?",
                positiveText = "Çıkış Yap"
            ) {
                sessionManager.logout()

                /**
                 * Güvenlik Önlemi: Task Stack Temizliği.
                 * FLAG_ACTIVITY_CLEAR_TASK ve NEW_TASK bayrakları, çıkış yapıldığında
                 * geri tuşuna basılarak eski oturuma dönülmesini (Backstack) engeller.
                 */
                val intent = Intent(this, RoleSelectionActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * UX Güvenliği (Back Press Override):
     * Android'in varsayılan geri tuşu davranışını ezer. Kullanıcının yanlışlıkla
     * uygulamadan çıkış yapmasını engellemek için bir onay diyaloğu sunar.
     * Bu durum kullanıcı deneyimini (User Experience) iyileştirir ve istenmeyen
     * oturum sonlandırmalarının önüne geçer.
     */
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmationDialog(
                    title = "Giriş Ekranına Dön",
                    message = "Giriş ekranına dönmek istediğinize emin misiniz?",
                    positiveText = "Evet, Dön",
                    negativeText = "Vazgeç"
                ) {
                    sessionManager.logout()
                    val intent = Intent(this@MainActivity, RoleSelectionActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
    }
}