package com.erenkadiroglu.saharez.ui.view

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.databinding.ActivityLoginBinding
import com.erenkadiroglu.saharez.data.local.SessionManager
import com.erenkadiroglu.saharez.data.remote.RetrofitClient
import com.erenkadiroglu.saharez.data.model.LoginRequest
import com.erenkadiroglu.saharez.data.model.LoginResponse
import retrofit2.Call
import retrofit2.Response

/**
 * LoginActivity: Uygulamanın kimlik doğrulama (Authentication) merkezidir.
 * Kullanıcı rollerine göre (ADMIN/PLAYER) arayüzü dinamik olarak temalandırır (Dynamic Theming).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val currentScreenRole = intent.getStringExtra(EXTRA_USER_ROLE) ?: "PLAYER"

        setupUIByRole(currentScreenRole)
        setupClickListeners(currentScreenRole)
    }

    private fun setupUIByRole(role: String) {
        val isAdmin = role == "ADMIN"
        binding.textWelcome.text = if (isAdmin) "Saha Görevlisi Girişi" else "Oyuncu Girişi"

        if (isAdmin) {
            val adminColor = Color.parseColor("#3B82F6")
            binding.lottieLogin.setAnimation(R.raw.anim_admin)
            binding.lottieLogin.scaleX = 1.0f
            binding.lottieLogin.scaleY = 1.0f
            binding.lottieLogin.playAnimation()

            binding.textWelcome.setTextColor(adminColor)
            binding.btnLogin.backgroundTintList = ColorStateList.valueOf(adminColor)
            binding.textRegister.setTextColor(adminColor)

            // YENİ: Checkbox rengini de role göre ayarla
            binding.cbRememberMe.buttonTintList = ColorStateList.valueOf(adminColor)

            binding.inputLayoutUsername.boxStrokeColor = adminColor
            binding.inputLayoutUsername.hintTextColor = ColorStateList.valueOf(adminColor)
            binding.inputLayoutPassword.boxStrokeColor = adminColor
            binding.inputLayoutPassword.hintTextColor = ColorStateList.valueOf(adminColor)
        } else {
            val playerColor = Color.parseColor("#10B981")
            binding.lottieLogin.setAnimation(R.raw.anim_player)
            binding.lottieLogin.scaleX = 1.7f
            binding.lottieLogin.scaleY = 1.7f
            binding.lottieLogin.playAnimation()

            binding.textWelcome.setTextColor(playerColor)
            binding.btnLogin.backgroundTintList = ColorStateList.valueOf(playerColor)
            binding.textRegister.setTextColor(playerColor)

            // YENİ: Checkbox rengini role göre ayarla
            binding.cbRememberMe.buttonTintList = ColorStateList.valueOf(playerColor)

            binding.inputLayoutUsername.boxStrokeColor = playerColor
            binding.inputLayoutUsername.hintTextColor = ColorStateList.valueOf(playerColor)
            binding.inputLayoutPassword.boxStrokeColor = playerColor
            binding.inputLayoutPassword.hintTextColor = ColorStateList.valueOf(playerColor)
        }
        binding.textRegister.visibility = View.VISIBLE
    }

    private fun setupClickListeners(currentScreenRole: String) {
        binding.textRegister.setOnClickListener {
            val websiteUrl = "https://saharez.xyz/saharez/register.php"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl))
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.editUsername.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                binding.editUsername.error = "Bu alan boş bırakılamaz"
                return@setOnClickListener
            }

            binding.btnLogin.isEnabled = false
            binding.btnLogin.text = "Giriş Yapılıyor..."

            val loginRequest = LoginRequest(username = username, password = password)

            RetrofitClient.instance.loginUser(loginRequest)
                .enqueue(object : retrofit2.Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Giriş Yap"

                        if (response.isSuccessful && response.body() != null) {
                            val data = response.body()!!
                            if (data.status == "success") {
                                val userDatabaseRole = data.role ?: "PLAYER"

                                if (userDatabaseRole != currentScreenRole) {
                                    val requiredScreen = if (userDatabaseRole == "ADMIN") "Saha Görevlisi" else "Oyuncu"
                                    Toast.makeText(this@LoginActivity, "Güvenlik İhlali: Lütfen $requiredScreen ekranından giriş yapın.", Toast.LENGTH_LONG).show()
                                    return
                                }

                                val finalName = data.full_name ?: username
                                val finalId = data.userId ?: 0

                                // YENİ: Checkbox durumunu oku
                                val isRememberMeChecked = binding.cbRememberMe.isChecked

                                // YENİ: Checkbox durumunu SessionManager'a gönder
                                sessionManager.saveLoginSession(true, currentScreenRole, finalId, finalName, isRememberMeChecked)
                                Toast.makeText(this@LoginActivity, data.message, Toast.LENGTH_SHORT).show()

                                val target = if (currentScreenRole == "ADMIN") MainActivity::class.java else BookingActivity::class.java
                                val intent = Intent(this@LoginActivity, target)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, data.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Sunucu hata verdi: " + response.code(), Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Giriş Yap"
                        Toast.makeText(this@LoginActivity, "Bağlantı Hatası", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }

    companion object {
        const val EXTRA_USER_ROLE = "EXTRA_USER_ROLE"
    }
}