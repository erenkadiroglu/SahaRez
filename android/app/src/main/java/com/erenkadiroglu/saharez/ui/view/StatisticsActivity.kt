package com.erenkadiroglu.saharez.ui.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.data.model.StatsResponse
import com.erenkadiroglu.saharez.data.remote.RetrofitClient
import com.erenkadiroglu.saharez.databinding.ActivityStatisticsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

/**
 * StatisticsActivity: Uygulamanın İş Zekası (Business Intelligence) Dashboard'udur.
 * Tesisin finansal performansını, doluluk oranlarını ve operasyonel metriklerini
 * sunucu tarafındaki analitik motorundan (Server-Side Logic) çekerek kullanıcıya
 * dinamik olarak raporlar.
 */
class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatisticsBinding
    private var currentDateFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        // Ekran ilk açıldığında "Tüm Zamanlar" verisi ile dashboard başlatılır.
        fetchStatsFromServer(null)
    }

    /**
     * Kullanıcı Deneyimi (UX): Filtreleme mekanizması.
     * Kullanıcı tarih seçerek belirli bir günün verilerini sorgulayabilir.
     * Bu işlem hem veritabanı sorgusunu daraltır (Performance) hem de analitik hassasiyeti artırır.
     */
    private fun setupClickListeners() {
        binding.btnFilterDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                val displayDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year)

                currentDateFilter = selectedDate
                binding.tvCurrentFilter.text = getString(R.string.stats_filter_date, displayDate)
                binding.btnClearFilter.visibility = View.VISIBLE

                fetchStatsFromServer(currentDateFilter)

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnClearFilter.setOnClickListener {
            currentDateFilter = null
            binding.tvCurrentFilter.text = getString(R.string.stats_filter_all_time)
            binding.btnClearFilter.visibility = View.GONE

            fetchStatsFromServer(null)
        }
    }

    /**
     * Data Layer & Networking:
     * Retrofit ile sunucuya asenkron (Thread-safe) API isteği atılır.
     * Gelen JSON verisi `StatsResponse` modeline map edilir.
     */
    private fun fetchStatsFromServer(dateFilter: String?) {
        // UI Feedback: Veri yüklenirken kullanıcıya geri bildirim verilir (Progress Bar)
        binding.progressBarStats.visibility = View.VISIBLE

        RetrofitClient.instance.getStats(dateFilter).enqueue(object : Callback<StatsResponse> {
            override fun onResponse(call: Call<StatsResponse>, response: Response<StatsResponse>) {
                binding.progressBarStats.visibility = View.GONE

                if (response.isSuccessful) {
                    val body = response.body()

                    // Defensive Programming (Savunmacı Programlama):
                    // API'den gelen null veya hatalı verilerin uygulamayı çökertmemesi (crash) için
                    // Elvis Operatörü (?:) ve Null Check yöntemleri kullanılmıştır.
                    if (body != null && body.status == "success" && body.data != null) {
                        val stats = body.data

                        // UI Binding: İşlenmiş veriler arayüz bileşenlerine aktarılır.
                        binding.textTotalCount.text = stats.totalBookings.toString()
                        binding.tvCourtRevenue.text = getString(R.string.stats_court_revenue, stats.totalBase ?: 0.0)
                        binding.tvCleatsRevenue.text = getString(R.string.stats_cleats_revenue, stats.totalCleats ?: 0.0)
                        binding.tvBuffetRevenue.text = getString(R.string.stats_buffet_revenue, stats.totalBuffet ?: 0.0)
                        binding.tvTotalRevenue.text = getString(R.string.stats_total_revenue, stats.grandTotal ?: 0.0)

                        // İleri seviye analitik metrikleri
                        binding.tvAvgRevenue.text = getString(R.string.stats_avg_revenue, (stats.avgRevenue ?: 0.0).toInt())
                        binding.tvCleatCount.text = getString(R.string.stats_cleat_count, stats.cleatCount ?: 0)
                        binding.tvAvgBuffet.text = getString(R.string.stats_avg_buffet, stats.avgBuffet ?: 0.0)
                        binding.tvSideIncomeRatio.text = getString(R.string.stats_side_income, stats.sideIncomeRatio ?: 0)

                        val popTime = if (stats.popularTime.isNullOrEmpty()) "-" else stats.popularTime
                        binding.tvPopularTime.text = getString(R.string.stats_popular_time, popTime)
                        binding.tvCancelRatio.text = getString(R.string.stats_cancel_ratio, stats.cancelRatio ?: 0)

                        // Doluluk oranı hesaplaması
                        if (stats.occupancyRate >= 0) {
                            binding.tvOccupancyRate.text = getString(R.string.stats_occupancy_rate, stats.occupancyRate)
                        } else {
                            binding.tvOccupancyRate.text = getString(R.string.stats_occupancy_rate_empty)
                        }

                        binding.textTotalCount.visibility = View.VISIBLE
                    } else {
                        resetUI()
                        Toast.makeText(this@StatisticsActivity, getString(R.string.stats_no_record), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@StatisticsActivity, getString(R.string.stats_server_error, response.code()), Toast.LENGTH_SHORT).show()
                }
            }

            // Hata Durumu Yönetimi: İnternet veya sunucu kaynaklı hataların kullanıcıya bildirilmesi
            override fun onFailure(call: Call<StatsResponse>, t: Throwable) {
                binding.progressBarStats.visibility = View.GONE
                Toast.makeText(this@StatisticsActivity, getString(R.string.stats_error, t.localizedMessage), Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * State Management (UI Sıfırlama):
     * Veri bulunamadığında veya hata durumunda ekranın temiz ve tutarlı kalmasını sağlar.
     */
    private fun resetUI() {
        binding.textTotalCount.text = "0"
        binding.tvCourtRevenue.text = getString(R.string.stats_court_revenue, 0.0)
        binding.tvCleatsRevenue.text = getString(R.string.stats_cleats_revenue, 0.0)
        binding.tvBuffetRevenue.text = getString(R.string.stats_buffet_revenue, 0.0)
        binding.tvTotalRevenue.text = getString(R.string.stats_total_revenue, 0.0)

        binding.tvAvgRevenue.text = getString(R.string.stats_avg_revenue, 0)
        binding.tvCleatCount.text = getString(R.string.stats_cleat_count, 0)
        binding.tvAvgBuffet.text = getString(R.string.stats_avg_buffet, 0.0)
        binding.tvSideIncomeRatio.text = getString(R.string.stats_side_income, 0)

        binding.tvPopularTime.text = getString(R.string.stats_popular_time, "-")
        binding.tvCancelRatio.text = getString(R.string.stats_cancel_ratio, 0)
        binding.tvOccupancyRate.text = getString(R.string.stats_occupancy_rate_empty)
    }
}