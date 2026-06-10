package com.erenkadiroglu.saharez.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.erenkadiroglu.saharez.R
import com.erenkadiroglu.saharez.data.model.TimeSlot

/**
 * BookingAdapter: Adapter Design Pattern (Adaptör Tasarım Deseni) uygulaması.
 * Veri kaynağındaki (Model: TimeSlot) bilgileri, RecyclerView'ın (View) anlayabileceği
 * formata dönüştürür. "Separation of Concerns" (Sorumlulukların ayrılması) prensibine
 * göre liste yönetimi ile arayüz mantığını birbirinden ayırır.
 */
class BookingAdapter(
    private val timeSlotList: List<TimeSlot>,
    private val currentUserId: Int, // Oturum açan kullanıcının ID'si (Kişisel sıra takibi için)
    private val onSlotClick: (TimeSlot, Int) -> Unit // High-order function: Tıklama olaylarını Activity'e bildirir.
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    /**
     * ViewHolder Pattern: Performans optimizasyonu için kullanılır.
     * View'ların (TextView, vb.) tekrar tekrar aranmasını (findViewById) engelleyerek,
     * liste kaydırılırken uygulamanın akıcı kalmasını sağlar (Rendering Performance).
     */
    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTimeSlot: TextView = itemView.findViewById(R.id.textTimeSlot)
        val viewStatus: View = itemView.findViewById(R.id.viewStatus)
        val textWaitingInfo: TextView = itemView.findViewById(R.id.textWaitingInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        // XML layout dosyasının View nesnesine dönüştürülmesi (Inflation)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_time_slot, parent, false)
        return BookingViewHolder(view)
    }

    /**
     * Data Binding: Model verisinin View bileşenlerine bağlandığı ana metod.
     * Burada "Business Logic" (İş Mantığı) ile UI (Görünüm) birleşir.
     */
    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val slot = timeSlotList[position]
        holder.textTimeSlot.text = slot.time

        // State Management: Seansın doluluk durumuna göre görsel durum yönetimi
        if (slot.isAvailable) {
            holder.viewStatus.setBackgroundResource(R.drawable.shape_circle_green)
            holder.textWaitingInfo.visibility = View.GONE
        } else {
            holder.viewStatus.setBackgroundResource(R.drawable.shape_circle_red)

            // Dynamic UI Generation: Bekleme listesi için dinamik durum gösterimi
            if (slot.waitingCount > 0) {
                holder.textWaitingInfo.visibility = View.VISIBLE

                // Algoritmik Kontrol: Kullanıcı bu seans için kuyrukta mı?
                val myIndex = slot.waitingUsers.indexOf(currentUserId)

                if (myIndex != -1) {
                    // Kullanıcı kuyruktaysa özel durum bildirimi (Mavi)
                    holder.textWaitingInfo.text = "⏳ Sıranız: ${myIndex + 1}"
                    holder.textWaitingInfo.setTextColor(Color.parseColor("#3B82F6"))
                } else {
                    // Kullanıcı kuyrukta değilse toplam sayı bildirimi (Turuncu)
                    holder.textWaitingInfo.text = "⏳ ${slot.waitingCount} Kişi"
                    holder.textWaitingInfo.setTextColor(Color.parseColor("#F59E0B"))
                }
            } else {
                holder.textWaitingInfo.visibility = View.GONE
            }
        }

        // Event Delegation (Callback): Tıklama olayını Activity seviyesine paslar.
        holder.itemView.setOnClickListener {
            onSlotClick(slot, position)
        }
    }

    // Listeleme performansını optimize etmek için toplam öğe sayısını döndürür.
    override fun getItemCount() = timeSlotList.size
}