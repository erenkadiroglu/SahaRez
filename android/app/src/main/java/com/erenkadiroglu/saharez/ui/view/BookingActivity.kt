package com.erenkadiroglu.saharez.ui.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.data.local.AppDatabase
import com.erenkadiroglu.saharez.data.local.SessionManager
import com.erenkadiroglu.saharez.data.model.Booking
import com.erenkadiroglu.saharez.data.model.BookingRequest
import com.erenkadiroglu.saharez.data.model.BookingResponse
import com.erenkadiroglu.saharez.data.model.PlayerPaymentRequest
import com.erenkadiroglu.saharez.data.remote.RetrofitClient
import com.erenkadiroglu.saharez.data.repository.BookingRepository
import com.erenkadiroglu.saharez.databinding.ActivityBookingBinding
import com.erenkadiroglu.saharez.data.model.PlayerPayment
import com.erenkadiroglu.saharez.data.model.TimeSlot
import com.erenkadiroglu.saharez.data.model.MatchAd
import com.erenkadiroglu.saharez.ui.adapter.BookingAdapter
import com.erenkadiroglu.saharez.ui.viewmodel.BookingViewModel
import com.erenkadiroglu.saharez.ui.viewmodel.BookingViewModelFactory
import com.erenkadiroglu.saharez.utils.NetworkMonitor
import com.erenkadiroglu.saharez.utils.NetworkResult
import com.erenkadiroglu.saharez.utils.showConfirmationDialog
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

/**
 * BookingActivity: Uygulamanın Merkezi Kullanıcı Arayüzü (View Layer).
 */
@SuppressLint("SetTextI18n", "NotifyDataSetChanged", "SpellCheckingInspection")
class BookingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: BookingAdapter
    private var timeList = mutableListOf<TimeSlot>()
    private var currentSelectedDate: String = ""

    private lateinit var viewModel: BookingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        initDate()
        loadTimeSlots()
        setupUIByRole()
        setupClickListeners()
        setupBackPress()

        setupViewModel()
        observeViewModel()

        val networkMonitor = NetworkMonitor(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isConnected.collect { internetGeldi ->
                    if (internetGeldi) {
                        viewModel.fetchDailyBookings(currentSelectedDate)
                        if (binding.layoutAds.isVisible) {
                            viewModel.fetchMatchAds(currentSelectedDate)
                        }
                    }
                }
            }
        }

        viewModel.fetchDailyBookings(currentSelectedDate)
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(this)
        val repository = BookingRepository(database.bookingDao(), database.adDao())
        val factory = BookingViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[BookingViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.dailyBookingsResponse.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { }
                is NetworkResult.Success -> {
                    timeList.forEach { slot ->
                        slot.isAvailable = true
                        slot.fullName = null
                        slot.phoneNo = null
                        slot.hasShuttle = false
                        slot.waitingCount = 0
                        slot.waitingUsers = emptyList()
                    }

                    result.data?.forEach { dbBooking ->
                        val index = timeList.indexOfFirst { it.time == dbBooking.timeSlot }
                        if (index != -1) {
                            val targetSlot = timeList[index]
                            targetSlot.isAvailable = false
                            targetSlot.fullName = dbBooking.fullName
                            targetSlot.phoneNo = dbBooking.phone
                            targetSlot.hasShuttle = (dbBooking.hasShuttle == 1)
                            targetSlot.waitingCount = dbBooking.waitingCount
                            targetSlot.waitingUsers = dbBooking.waitingUsers

                            if (!dbBooking.players.isNullOrEmpty()) {
                                targetSlot.paidPlayers.clear()
                                dbBooking.players.forEach { playerResponse ->
                                    targetSlot.paidPlayers.add(
                                        PlayerPayment(
                                            name = playerResponse.name ?: "Bilinmeyen",
                                            rentedCleats = playerResponse.rentedCleats,
                                            buffetExpense = playerResponse.buffetExpense
                                        )
                                    )
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this@BookingActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.createBookingResponse.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { Toast.makeText(this@BookingActivity, "Kaydediliyor...", Toast.LENGTH_SHORT).show() }
                is NetworkResult.Success -> {
                    Toast.makeText(this@BookingActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                    viewModel.fetchDailyBookings(currentSelectedDate)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this@BookingActivity, result.message, Toast.LENGTH_LONG).show()
                    viewModel.fetchDailyBookings(currentSelectedDate)
                }
            }
        }

        viewModel.matchAdsResponse.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { }
                is NetworkResult.Success -> {
                    updateAdsUI(result.data ?: emptyList())
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this@BookingActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.createAdResponse.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> { Toast.makeText(this@BookingActivity, "İlan işleniyor...", Toast.LENGTH_SHORT).show() }
                is NetworkResult.Success -> {
                    Toast.makeText(this@BookingActivity, "İlan Başarıyla Kaydedildi!", Toast.LENGTH_SHORT).show()
                    viewModel.fetchMatchAds(currentSelectedDate)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this@BookingActivity, result.message, Toast.LENGTH_LONG).show()
                    viewModel.fetchDailyBookings(currentSelectedDate)
                }
            }
        }
    }

    private fun initDate() {
        val calendar = Calendar.getInstance()
        currentSelectedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        binding.tvDateFilter.text = currentSelectedDate
    }

    private fun loadTimeSlots() {
        timeList.clear()
        timeList.addAll(listOf(
            TimeSlot("13.00 - 14.00", "05458448676"),
            TimeSlot("14.00 - 15.00", "05458448676"),
            TimeSlot("15.00 - 16.00", "05458448676"),
            TimeSlot("16.00 - 17.00", "05458448676"),
            TimeSlot("17.00 - 18.00", "05458448676"),
            TimeSlot("18.00 - 19.00", "05458448676"),
            TimeSlot("19.00 - 20.00", "05458448676"),
            TimeSlot("20.00 - 21.00", "05458448676"),
            TimeSlot("21.00 - 22.00", "05458448676"),
            TimeSlot("22.00 - 23.00", "05458448676"),
            TimeSlot("23.00 - 00.00", "05458448676")
        ))

        binding.rvTimeSlots.layoutManager = LinearLayoutManager(this)
        adapter = BookingAdapter(timeList, sessionManager.getUserId()) { clickedSlot, position ->
            handleSlotClick(clickedSlot, position)
        }
        binding.rvTimeSlots.adapter = adapter
    }

    private fun setupUIByRole() {
        val role = sessionManager.getUserRole()
        if (role == "ADMIN") {
            binding.llLegend.isVisible = false
            binding.tvCall.isVisible = false
        }
    }

    private fun handleSlotClick(slot: TimeSlot, position: Int) {
        val role = sessionManager.getUserRole()

        if (role == "ADMIN") {
            showBookingDialog(slot, position)
        } else {
            if (!slot.isAvailable) {
                val isOwner = checkOwnership(sessionManager.getUserName(), slot.fullName)
                if (isOwner) {
                    showCreateAdDialog(slot)
                } else {
                    showConfirmationDialog(
                        title = "Bekleme Listesine Katıl",
                        message = "${slot.time} seansı şu an dolu. Eğer bu randevu iptal olursa sıraya girmek ister misiniz?",
                        positiveText = "Evet, Sıraya Ekle",
                        negativeText = "Vazgeç"
                    ) {
                        joinWaitingListToDatabase(slot)
                    }
                }
            } else {
                AlertDialog.Builder(this@BookingActivity)
                    .setTitle("Randevu Al")
                    .setMessage("Bu seans şu an müsait. Randevu almak için lütfen saha görevlisini arayın.")
                    .setPositiveButton("Şimdi Ara") { _, _ ->
                        binding.tvCall.performClick()
                    }
                    .setNegativeButton("Kapat", null)
                    .show()
            }
        }
    }

    private fun joinWaitingListToDatabase(slot: TimeSlot) {
        val userId = sessionManager.getUserId()
        if (userId == 0) {
            Toast.makeText(this@BookingActivity, "Kimlik hatası! Lütfen çıkış yapıp tekrar girin.", Toast.LENGTH_SHORT).show()
            return
        }
        RetrofitClient.instance.joinWaitingList(userId, currentSelectedDate, slot.time)
            .enqueue(object : Callback<BookingResponse> {
                override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@BookingActivity, response.body()!!.message, Toast.LENGTH_LONG).show()
                        viewModel.fetchDailyBookings(currentSelectedDate)
                    }
                }
                override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                    Toast.makeText(this@BookingActivity, "Bağlantı hatası: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun showBookingDialog(slot: TimeSlot, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.layout_dialog_booking, null)

        val tvTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val etFullName = dialogView.findViewById<EditText>(R.id.etFullName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val rbServissiz = dialogView.findViewById<RadioButton>(R.id.rbPaid)
        val rbServisli = dialogView.findViewById<RadioButton>(R.id.rbUnpaid)
        rbServissiz.text = "Servissiz (190₺)"
        rbServisli.text = "Servisli (250₺)"

        val etPlayerName = dialogView.findViewById<EditText>(R.id.etPlayerName)
        val cbCleats = dialogView.findViewById<CheckBox>(R.id.cbCleats)
        val etBuffetExpense = dialogView.findViewById<EditText>(R.id.etBuffetExpense)
        val btnAddPlayer = dialogView.findViewById<Button>(R.id.btnAddPlayer)
        val llPlayersContainer = dialogView.findViewById<LinearLayout>(R.id.llPlayersContainer)

        val isEditing = !slot.isAvailable
        val currentPaidPlayers = slot.paidPlayers.toMutableList()

        val tvToplamTutar = TextView(this@BookingActivity).apply {
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor("#2E7D32".toColorInt())
            setPadding(0, 20, 0, 20)
            gravity = Gravity.CENTER
        }
        llPlayersContainer.addView(tvToplamTutar, 0)

        fun hesaplaToplam() {
            val basePrice = if (rbServisli.isChecked) 250.0 else 190.0
            val total = currentPaidPlayers.sumOf { player ->
                var playerCost = basePrice
                if (player.rentedCleats) playerCost += 50.0
                playerCost += player.buffetExpense.toDouble()
                playerCost
            }
            tvToplamTutar.text = "TOPLAM TUTAR: ${total.toInt()}₺"
        }

        fun updatePlayersUI() {
            while (llPlayersContainer.childCount > 1) {
                llPlayersContainer.removeViewAt(1)
            }
            val basePrice = if (rbServisli.isChecked) 250 else 190

            currentPaidPlayers.forEachIndexed { index, player ->
                val rowLayout = LinearLayout(this@BookingActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    setPadding(0, 16, 0, 16)
                }

                var playerTotal = basePrice
                var extrasText = ""

                if (player.rentedCleats || player.buffetExpense > 0) {
                    val extrasList = mutableListOf<String>()
                    if (player.rentedCleats) { extrasList.add("Krampon: 50₺"); playerTotal += 50 }
                    if (player.buffetExpense > 0) { extrasList.add("Büfe: ${player.buffetExpense}₺"); playerTotal += player.buffetExpense }
                    extrasText = " + " + extrasList.joinToString(" + ")
                }

                val tvName = TextView(this@BookingActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    text = android.text.Html.fromHtml("• ${player.name}<br>  <small><font color='#757575'>Hesap: ${basePrice}₺$extrasText = <b>${playerTotal}₺</b></font></small>", android.text.Html.FROM_HTML_MODE_COMPACT)
                    textSize = 15f
                    setTextColor(Color.BLACK)
                }

                val tvRemove = TextView(this@BookingActivity).apply {
                    text = "SİL"
                    setTextColor(Color.RED)
                    textSize = 14f
                    setPadding(16, 0, 16, 0)
                    isClickable = true
                    setOnClickListener {
                        showConfirmationDialog("Oyuncuyu Sil", "'${player.name}' isimli oyuncuyu ödeme listesinden silmek istediğinize emin misiniz?", "Evet, Sil", "Vazgeç") {
                            currentPaidPlayers.removeAt(index)
                            updatePlayersUI()
                            hesaplaToplam()
                        }
                    }
                }
                rowLayout.addView(tvName)
                rowLayout.addView(tvRemove)
                llPlayersContainer.addView(rowLayout)
            }
        }

        rbServisli.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { updatePlayersUI(); hesaplaToplam() } }
        rbServissiz.setOnCheckedChangeListener { _, isChecked -> if (isChecked) { updatePlayersUI(); hesaplaToplam() } }

        val selectedDateText = binding.tvDateFilter.text.toString()

        if (isEditing) {
            tvTitle.text = "Randevu Düzenle: \n$selectedDateText  |  ${slot.time}"
            etFullName.setText(slot.fullName)
            etPhone.setText(slot.phoneNo)
            if (slot.hasShuttle) rbServisli.isChecked = true else rbServissiz.isChecked = true
        } else {
            tvTitle.text = "Randevu Oluştur: \n$selectedDateText  |  ${slot.time}"
            rbServissiz.isChecked = true
        }

        updatePlayersUI()
        hesaplaToplam()

        btnAddPlayer.setOnClickListener {
            val name = etPlayerName.text.toString().trim()
            val buffetStr = etBuffetExpense.text.toString().trim()
            val buffetExpense = if (buffetStr.isNotEmpty()) buffetStr.toInt() else 0
            val rentedCleats = cbCleats.isChecked

            if (name.isNotEmpty()) {
                currentPaidPlayers.add(PlayerPayment(name, rentedCleats, buffetExpense))
                etPlayerName.text.clear()
                cbCleats.isChecked = false
                etBuffetExpense.text.clear()
                updatePlayersUI()
                hesaplaToplam()
            } else {
                Toast.makeText(this@BookingActivity, "Lütfen oyuncu adı girin!", Toast.LENGTH_SHORT).show()
            }
        }

        val dialogBuilder = AlertDialog.Builder(this@BookingActivity)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Kaydet") { dialog, _ ->
                val fullName = etFullName.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (fullName.isNotEmpty() && phone.isNotEmpty()) {
                    val requestPlayers = currentPaidPlayers.map {
                        PlayerPaymentRequest(it.name, it.rentedCleats, it.buffetExpense)
                    }

                    val newBooking = Booking(
                        userId = sessionManager.getUserId(),
                        fullName = fullName,
                        phone = phone,
                        matchDate = currentSelectedDate,
                        timeSlot = slot.time,
                        hasShuttle = if (rbServisli.isChecked) 1 else 0,
                        status = "PENDING"
                    )

                    val bookingRequest = BookingRequest(
                        user_id = sessionManager.getUserId(),
                        full_name = fullName,
                        phone = phone,
                        match_date = currentSelectedDate,
                        time_slot = slot.time,
                        has_shuttle = if (rbServisli.isChecked) 1 else 0,
                        players = requestPlayers
                    )

                    timeList[position].apply {
                        this.isAvailable = false
                        this.fullName = fullName
                        this.phoneNo = phone
                        this.hasShuttle = rbServisli.isChecked
                        this.paidPlayers.clear()
                        this.paidPlayers.addAll(currentPaidPlayers)
                    }
                    adapter.notifyItemChanged(position)

                    viewModel.createNewBooking(newBooking, bookingRequest)
                    dialog.dismiss()

                } else {
                    Toast.makeText(this@BookingActivity, "Lütfen tüm bilgileri doldurun!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Vazgeç") { dialog, _ ->
                dialog.dismiss()
            }

        // --- DEĞİŞEN İPTAL BLOĞU ---
        if (isEditing) {
            dialogBuilder.setNeutralButton("Randevuyu İptal Et") { mainDialog, _ ->
                showConfirmationDialog("Randevu İptali", "Bu randevuyu tamamen silmek istediğinize emin misiniz?", "Evet, Sil", "Kapat") {

                    // İyimser Güncelleme: Ekrandan anında temizliyoruz
                    timeList[position].apply {
                        this.isAvailable = true
                        this.fullName = null
                        this.phoneNo = null
                        this.hasShuttle = false
                        this.paidPlayers = mutableListOf()
                        this.waitingCount = 0
                        this.waitingUsers = emptyList()
                    }
                    adapter.notifyItemChanged(position)

                    // Sunucuya silme isteği atıyoruz
                    RetrofitClient.instance.deleteBooking(currentSelectedDate, slot.time)
                        .enqueue(object : Callback<BookingResponse> {
                            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                                if (response.isSuccessful && response.body()?.status == "success") {
                                    Toast.makeText(this@BookingActivity, "Randevu sistemden tamamen silindi.", Toast.LENGTH_SHORT).show()
                                    // Veritabanı ve ekrandaki tutarlılığı sağlamak için listeyi yeniliyoruz
                                    viewModel.fetchDailyBookings(currentSelectedDate)
                                } else {
                                    Toast.makeText(this@BookingActivity, "Silme başarısız: Sunucu hatası", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                                Toast.makeText(this@BookingActivity, "Silme başarısız: İnternet bağlantınızı kontrol edin.", Toast.LENGTH_SHORT).show()
                            }
                        })

                    mainDialog.dismiss()
                }
            }
        }

        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable("#1E293B".toColorInt()))
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor("#10B981".toColorInt())
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor("#94A3B8".toColorInt())
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor("#EF4444".toColorInt())
    }

    private fun showCreateAdDialog(slot: TimeSlot) {
        val dialogView = layoutInflater.inflate(R.layout.layout_dialog_create_ad, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvAdDialogTitle)
        val rgAdType = dialogView.findViewById<android.widget.RadioGroup>(R.id.rgAdType)
        val rbMissingPlayer = dialogView.findViewById<RadioButton>(R.id.rbMissingPlayer)
        val llMissingDetails = dialogView.findViewById<LinearLayout>(R.id.llMissingPlayerDetails)
        val etMissingCount = dialogView.findViewById<EditText>(R.id.etMissingCount)
        val etMissingPosition = dialogView.findViewById<EditText>(R.id.etMissingPosition)

        tvTitle.text = "İlan Ver: ${slot.time}"

        rgAdType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbMissingPlayer) {
                llMissingDetails.isVisible = true
            } else {
                llMissingDetails.isVisible = false
            }
        }

        AlertDialog.Builder(this@BookingActivity)
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("İlanı Yayınla") { dialog, _ ->
                val adType = if (rbMissingPlayer.isChecked) "OYUNCU EKSİK" else "RAKİP ARANIYOR"
                var missingCount: String? = null
                var missingPosition: String? = null

                if (rbMissingPlayer.isChecked) {
                    missingCount = etMissingCount.text.toString().trim()
                    missingPosition = etMissingPosition.text.toString().trim()

                    if (missingCount.isNullOrEmpty() || missingPosition.isNullOrEmpty()) {
                        Toast.makeText(this@BookingActivity, "Lütfen eksik bilgilerini doldurun!", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                }

                val newAd = MatchAd(
                    userId = sessionManager.getUserId(),
                    matchDate = currentSelectedDate,
                    timeSlot = slot.time,
                    creatorName = sessionManager.getUserName(),
                    creatorPhone = "05458448676",
                    type = adType,
                    missingCount = missingCount,
                    missingPositions = missingPosition,
                    isSynced = false
                )

                viewModel.createNewMatchAd(newAd)
                dialog.dismiss()
            }
            .setNegativeButton("Vazgeç") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showEditAdDialog(ad: MatchAd) {
        Toast.makeText(this, "Düzenleme modu yakında güncellenecektir.", Toast.LENGTH_SHORT).show()
    }

    private fun updateAdsUI(adsList: List<MatchAd>) {
        val container = binding.llAdsContainer
        container.removeAllViews()

        val colorSurfaceVariant = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant, Color.DKGRAY)
        val colorTextPrimary = MaterialColors.getColor(this, android.R.attr.textColorPrimary, Color.WHITE)

        if (adsList.isEmpty()) {
            val tvEmpty = TextView(this@BookingActivity).apply {
                text = "Şu anda aktif bir ilan bulunmuyor."
                textSize = 16f
                gravity = Gravity.CENTER
                setPadding(0, 64, 0, 0)
                setTextColor(colorTextPrimary)
            }
            container.addView(tvEmpty)
            return
        }

        val role = sessionManager.getUserRole()

        adsList.forEach { ad ->
            val isCreator = checkOwnership(sessionManager.getUserName(), ad.creatorName)
            val isAdmin = role == "ADMIN"

            val cardLayout = LinearLayout(this@BookingActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 48, 48, 48)
                setBackgroundResource(R.drawable.bg_edit_text)
                backgroundTintList = ColorStateList.valueOf(colorSurfaceVariant)
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 0, 24)
                layoutParams = params
                elevation = 4f
            }

            val tvType = TextView(this@BookingActivity).apply {
                text = ad.type
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(if (ad.type == "OYUNCU EKSİK") "#EF4444".toColorInt() else "#10B981".toColorInt())
            }

            val tvDetails = TextView(this@BookingActivity).apply {
                var detailsText = "Seans: ${ad.timeSlot}\nİlan Sahibi: ${ad.creatorName}"
                if (ad.type == "OYUNCU EKSİK") {
                    detailsText += "\n\nEksik Sayısı: ${ad.missingCount}\nMevki: ${ad.missingPositions}"
                }
                text = detailsText
                textSize = 15f
                setTextColor(colorTextPrimary)
                setPadding(0, 16, 0, 16)
            }

            cardLayout.addView(tvType)
            cardLayout.addView(tvDetails)

            val buttonsLayout = LinearLayout(this@BookingActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.END
            }

            if (!isCreator || isAdmin) {
                val btnContact = Button(this@BookingActivity).apply {
                    text = "İletişime Geç"
                    backgroundTintList = ColorStateList.valueOf("#10B981".toColorInt())
                    setTextColor(Color.WHITE)
                    val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    if (isAdmin) btnParams.setMargins(0, 0, 16, 0)
                    layoutParams = btnParams

                    setOnClickListener {
                        val targetPhone = ad.creatorPhone
                        if (targetPhone.isNotEmpty()) {
                            var cleanPhone = targetPhone.replace(" ", "").trim()
                            if (cleanPhone.startsWith("0")) cleanPhone = "+90" + cleanPhone.substring(1)
                            else if (!cleanPhone.startsWith("+")) cleanPhone = "+90$cleanPhone"

                            val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$cleanPhone") }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@BookingActivity, "Telefon numarası bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                buttonsLayout.addView(btnContact)
            }

            if (isAdmin || isCreator) {
                val btnEdit = Button(this@BookingActivity).apply {
                    text = "Düzenle"
                    backgroundTintList = ColorStateList.valueOf("#3B82F6".toColorInt())
                    setTextColor(Color.WHITE)
                    val btnParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                    btnParams.setMargins(0, 0, 16, 0)
                    layoutParams = btnParams
                    setOnClickListener { showEditAdDialog(ad) }
                }

                val btnDelete = Button(this@BookingActivity).apply {
                    text = "Sil"
                    backgroundTintList = ColorStateList.valueOf("#EF4444".toColorInt())
                    setTextColor(Color.WHITE)
                    setOnClickListener {
                        showConfirmationDialog("İlanı Sil", "Bu ilanı tamamen silmek istediğinize emin misiniz?", "Evet, Sil") {
                            Toast.makeText(this@BookingActivity, "İlan silme özelliği yakında eklenecek.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                buttonsLayout.addView(btnEdit)
                buttonsLayout.addView(btnDelete)
            }

            cardLayout.addView(buttonsLayout)
            container.addView(cardLayout)
        }
    }

    private fun setupClickListeners() {
        binding.tvDateFilter.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this@BookingActivity, { _, year, month, day ->
                currentSelectedDate = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year)
                binding.tvDateFilter.text = currentSelectedDate

                viewModel.fetchDailyBookings(currentSelectedDate)
                if(binding.layoutAds.isVisible) viewModel.fetchMatchAds(currentSelectedDate)

            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvSessions.setOnClickListener {
            binding.rvTimeSlots.isVisible = true
            binding.layoutAds.isVisible = false
            if (sessionManager.getUserRole() != "ADMIN") binding.llLegend.isVisible = true

            binding.tvSessions.setBackgroundResource(R.drawable.bg_edit_text)
            binding.tvSessions.backgroundTintList = ColorStateList.valueOf("#10B981".toColorInt())
            binding.tvSessions.setTextColor(Color.WHITE)

            binding.tvAds.setBackgroundResource(R.drawable.shape_rectangle_border)
            binding.tvAds.backgroundTintList = ColorStateList.valueOf("#111827".toColorInt())
            binding.tvAds.setTextColor("#94A3B8".toColorInt())
        }

        binding.tvAds.setOnClickListener {
            binding.rvTimeSlots.isVisible = false
            binding.layoutAds.isVisible = true
            binding.llLegend.isVisible = false

            binding.tvAds.setBackgroundResource(R.drawable.bg_edit_text)
            binding.tvAds.backgroundTintList = ColorStateList.valueOf("#10B981".toColorInt())
            binding.tvAds.setTextColor(Color.WHITE)

            binding.tvSessions.setBackgroundResource(R.drawable.shape_rectangle_border)
            binding.tvSessions.backgroundTintList = ColorStateList.valueOf("#111827".toColorInt())
            binding.tvSessions.setTextColor("#94A3B8".toColorInt())

            viewModel.fetchMatchAds(currentSelectedDate)
        }

        binding.tvCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:05458448676") })
        }
    }

    private fun setupBackPress() {
        if (sessionManager.getUserRole() == "PLAYER") {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.layoutAds.isVisible) { binding.tvSessions.performClick(); return }

                    showConfirmationDialog("Giriş Ekranına Dön", "Giriş ekranına dönmek istediğinize emin misiniz?", "Evet, Dön", "Vazgeç") {
                        sessionManager.logout()
                        startActivity(Intent(this@BookingActivity, RoleSelectionActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
                        finish()
                    }
                }
            })
        }
    }

    private fun checkOwnership(sessionName: String, slotName: String?): Boolean {
        if (slotName.isNullOrBlank()) return false
        return sessionName == slotName
    }
}