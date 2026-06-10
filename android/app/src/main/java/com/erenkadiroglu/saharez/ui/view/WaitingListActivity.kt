package com.erenkadiroglu.saharez.ui.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.databinding.ActivityWaitingListBinding
import com.erenkadiroglu.saharez.data.remote.RetrofitClient
import com.erenkadiroglu.saharez.data.model.WaitingListResponse
import com.erenkadiroglu.saharez.data.model.BookingResponse
import com.erenkadiroglu.saharez.data.model.WaitingPlayer
import com.erenkadiroglu.saharez.utils.showConfirmationDialog
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * WaitingListActivity: Operasyonel Yönetim Ekranı.
 * Saha yönetimi için bekleme listesindeki oyuncuların durumunu takip eder ve yönetir.
 * Dinamik olarak liste oluşturma ve harici uygulamalarla (Telefon) etkileşim sağlayan
 * bir "Admin-Client" arayüzü sunar.
 */
class WaitingListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWaitingListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWaitingListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchWaitingList()
    }

    /**
     * API Networking (Asynchronous):
     * Sunucudan bekleme listesini çeker. "Loading" durumu ile kullanıcıya
     * işlemin arka planda gerçekleştiğine dair görsel geri bildirim sağlar.
     */
    private fun fetchWaitingList() {
        val container = binding.llWaitingPlayersContainer
        container.removeAllViews()

        // UI Feedback: Yüklenme esnasında boş ekran yerine bilgi mesajı gösterimi
        val tvLoading = TextView(this).apply {
            text = "Bekleyenler listesi yükleniyor..."
            textSize = 16f
            setPadding(0, 32, 0, 0)
            setTextColor(getColor(R.color.light_yazi_ikincil))
        }
        container.addView(tvLoading)

        RetrofitClient.instance.getWaitingList().enqueue(object : Callback<WaitingListResponse> {
            override fun onResponse(call: Call<WaitingListResponse>, response: Response<WaitingListResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.status == "success") {
                        val list = data.data ?: emptyList()
                        buildWaitingListUI(list) // Veri başarılı gelirse listeyi çiz
                    } else {
                        // Error Handling: Sunucudan gelen mantıksal hataların (iş kuralı hataları) yönetimi
                        showError("Hata: ${data.message ?: "Bilinmeyen Sunucu Hatası"}")
                    }
                } else {
                    showError("Sunucu hatası: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WaitingListResponse>, t: Throwable) {
                // Connectivity Handling: Ağ hatalarının kullanıcıya şeffaf bildirilmesi
                showError("Bağlantı hatası: ${t.message}")
            }
        })
    }

    /**
     * API DELETE Operation:
     * Bekleme listesinden oyuncuyu çıkartır. İstek başarılı olursa liste otomatik yenilenir.
     */
    private fun deletePlayerFromDatabase(playerId: Int) {
        RetrofitClient.instance.deleteWaitingPlayer(playerId).enqueue(object : Callback<BookingResponse> {
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    if (data.status == "success") {
                        Toast.makeText(this@WaitingListActivity, data.message, Toast.LENGTH_SHORT).show()
                        fetchWaitingList() // Veri tutarlılığı (Data Consistency) için listeyi güncelle
                    } else {
                        Toast.makeText(this@WaitingListActivity, data.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                Toast.makeText(this@WaitingListActivity, "Hata: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * UI State Management: Hata durumunda boş ekran yerine kullanıcıyı bilgilendiren state.
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        val container = binding.llWaitingPlayersContainer
        container.removeAllViews()

        val tvError = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(0, 32, 0, 0)
            setTextColor(android.graphics.Color.RED)
        }
        container.addView(tvError)
    }

    /**
     * Dynamic UI Generation (Programmatic View Injection):
     * Listeyi XML'de değil, çalışma zamanında inflate ederek dinamik olarak oluşturur.
     * Bu yöntem, listenin içeriğinin belirsiz olduğu veya çok sık değiştiği durumlarda
     * esnek bir UI yapısı sunar.
     */
    private fun buildWaitingListUI(waitingList: List<WaitingPlayer>) {
        val container = binding.llWaitingPlayersContainer
        container.removeAllViews()

        // Empty State: Liste boşsa kullanıcıya "Boş" olduğunu belirtmek kullanıcı deneyimi (UX) için önemlidir.
        if (waitingList.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "Şu anda bekleyen oyuncu bulunmuyor."
                textSize = 16f
                setPadding(0, 32, 0, 0)
                setTextColor(getColor(R.color.light_yazi_ikincil))
            }
            container.addView(tvEmpty)
            return
        }

        val inflater = LayoutInflater.from(this)

        waitingList.forEachIndexed { index, player ->
            val itemView = inflater.inflate(R.layout.item_waiting_list, container, false)

            val tvName = itemView.findViewById<TextView>(R.id.tvWaitingName)
            val tvSession = itemView.findViewById<TextView>(R.id.tvWaitingTimeSlot)
            val tvPhone = itemView.findViewById<TextView>(R.id.tvWaitingPhone)
            val btnCall = itemView.findViewById<MaterialButton>(R.id.btnCall)
            val btnDelete = itemView.findViewById<MaterialButton>(R.id.btnDelete)

            tvName.text = "${index + 1}. Sıra: ${player.name}"
            tvSession.text = "Seans: ${player.timeSlot}"
            tvPhone.text = "Tel: ${player.phone}"

            // Implicit Intent: Cihazın yerel "Arama" uygulamasına veriyi paslar.
            btnCall.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${player.phone}")
                }
                startActivity(intent)
            }

            // UX Security: Silme işlemi geri alınamaz olduğu için onay diyaloğu (Confirmation Dialog) ile korunur.
            btnDelete.setOnClickListener {
                showConfirmationDialog(
                    title = "Listeden Sil",
                    message = "'${player.name}' isimli oyuncuyu bekleme listesinden silmek istediğinize emin misiniz?",
                    positiveText = "Evet, Sil",
                    negativeText = "Vazgeç"
                ) {
                    deletePlayerFromDatabase(player.id)
                }
            }

            container.addView(itemView)
        }
    }
}