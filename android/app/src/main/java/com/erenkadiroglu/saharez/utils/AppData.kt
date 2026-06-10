package com.erenkadiroglu.saharez.utils

import com.erenkadiroglu.saharez.data.model.TimeSlot

/**
 * AppData: Singleton Design Pattern uygulaması.
 * * Uygulama yaşam döngüsü boyunca verilerin bellekte (RAM) kalıcı tutulmasını sağlayan
 * bir "In-memory Data Store" (Bellek İçi Veri Deposu) görevini üstlenir.
 * * * Amaç: Uygulamanın farklı modülleri arasında paylaşılan verilerin merkezi bir noktadan
 * yönetilmesini (Single Source of Truth) sağlamak ve tekrar eden nesne oluşturma maliyetini azaltmaktır.
 */
object AppData {

    // Global State: Uygulamanın farklı katmanlarından erişilebilen paylaşılan veri havuzu.
    var globalTimeSlots = mutableListOf<TimeSlot>()

    /**
     * Lazy Initialization (Tembel Başlatma):
     * * Uygulama kaynaklarının (Memory) gereksiz yere tüketilmesini engeller.
     * * Veri yapısı sadece ilk ihtiyaç duyulduğunda başlatılır (on-demand), bu da
     * uygulama açılış süresini optimize eder.
     */
    fun initTimeSlots() {
        if (globalTimeSlots.isEmpty()) {
            globalTimeSlots = mutableListOf(
                TimeSlot("13.00 - 14.00", "05458448676"),
                TimeSlot("14.00 - 15.00", "05458448676"),
                TimeSlot("15.00 - 16.00", "05458448676"),
                TimeSlot("16.00 - 17.00", "05458448676"),
                TimeSlot("17.00 - 18.00", "05458448676"),
                TimeSlot("18.00 - 19.00", "05458448676"),
                TimeSlot("19.00 - 20.00", "05458448676"),
                TimeSlot("20.00 - 21.00", "05458448676"),
                TimeSlot("21.00 - 22.00", "05458448676"),
                TimeSlot("23.00 - 00.00", "05458448676")
            )
        }
    }
}