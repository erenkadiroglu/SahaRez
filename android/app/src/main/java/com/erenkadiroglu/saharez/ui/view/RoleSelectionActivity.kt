package com.erenkadiroglu.saharez.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.databinding.ActivityRoleSelectionBinding
import com.erenkadiroglu.saharez.data.local.SessionManager
import com.erenkadiroglu.saharez.utils.showConfirmationDialog

/**
 * RoleSelectionActivity: Uygulamanın giriş noktası (Entry Point).
 * Uygulamanın kimlik doğrulama sürecini yönetir. Kullanıcının daha önce giriş yapıp
 * yapmadığını (Persistence Layer) kontrol ederek, gereksiz login ekranlarını atlar
 * ve rol bazlı yönlendirme yapar.
 */
class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        /**
         * 1. AŞAMA: Oturum Doğrulama (Authentication Persistence).
         * Uygulama başladığında mevcut bir oturum var mı kontrol edilir.
         * Eğer varsa, kullanıcıyı tekrar giriş yapmaya zorlamadan doğrudan kendi
         * ana paneline yönlendiririz (UX iyileştirmesi).
         */
        if (sessionManager.isLoggedIn()) {
            val savedRole = sessionManager.getUserRole()

            // Rol bazlı dinamik yönlendirme (Routing Logic)
            val targetActivity = if (savedRole == "ADMIN") {
                MainActivity::class.java
            } else {
                BookingActivity::class.java
            }

            val intent = Intent(this, targetActivity)
            // CLEAR_TASK: Yönlendirme yapıldığında giriş ekranını bellekten silerek,
            // geri tuşuyla çıkış yapılmasını engeller.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        // Eğer oturum yoksa arayüz çizilir
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupVideoBackground()
        setupClickListeners()
        setupBackPress()
    }

    override fun onResume() {
        super.onResume()
        // Uygulama arka plana atılıp geri dönüldüğünde videonun devamlılığını sağlar.
        if (::binding.isInitialized && !binding.videoBackground.isPlaying) {
            binding.videoBackground.start()
        }
    }

    /**
     * Görsel Engagement Katmanı:
     * Uygulamanın estetik kalitesini artıran ve kullanıcıyı karşılayan atmosferik video arka plan.
     */
    private fun setupVideoBackground() {
        val videoPath = "android.resource://" + packageName + "/" + R.raw.bg_stadium
        binding.videoBackground.setVideoURI(Uri.parse(videoPath))

        binding.videoBackground.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f) // Arka plan videosu olduğu için sesi sessize alınmıştır.
        }
        binding.videoBackground.start()
    }

    /**
     * Shared Element Transition (Paylaşılan Öğe Geçişi):
     * İki ekran arasında geçiş yaparken nesnelerin (kartların) sanki bir sonraki
     * ekrana büyüyerek gidiyormuş gibi görünmesini sağlar.
     * Bu, modern Android tasarım standartlarında "yüksek kaliteli kullanıcı deneyimi" olarak kabul edilir.
     */
    private fun setupClickListeners() {
        binding.cardPlayer.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(LoginActivity.EXTRA_USER_ROLE, "PLAYER")

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair(binding.cardPlayer as View, "shared_background")
            )
            startActivity(intent, options.toBundle())
        }

        binding.cardAdmin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra(LoginActivity.EXTRA_USER_ROLE, "ADMIN")

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                Pair(binding.cardAdmin as View, "shared_background")
            )
            startActivity(intent, options.toBundle())
        }
    }

    /**
     * UX Güvenliği (Exit Confirmation):
     * Uygulamadan çıkış yapmadan önce son bir onay alarak, kullanıcının
     * yanlışlıkla uygulamadan atılmasını engeller.
     */
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showConfirmationDialog(
                    title = "Uygulamadan Çık",
                    message = "SahaRez'den çıkış yapmak istediğinize emin misiniz?",
                    positiveText = "Çıkış Yap",
                    negativeText = "Vazgeç"
                ) {
                    finish()
                }
            }
        })
    }
}