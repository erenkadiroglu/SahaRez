package com.erenkadiroglu.saharez.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.erenkadiroglu.saharez.data.repository.BookingRepository

/**
 * BookingViewModelFactory: Factory Design Pattern (Fabrika Tasarım Deseni).
 * * Android mimarisinde ViewModel'ların constructor (yapıcı) metoduna doğrudan Repository gibi
 * bağımlılıklar (dependencies) geçilemez. Bu sınıf, bir "Fabrika" görevi görerek:
 * 1. ViewModel'ın bağımlılıklarını (Repository) alır.
 * 2. ViewModel örneğini (instance) oluşturur.
 * 3. Bu işlemi Activity'den bağımsız (decoupled) şekilde gerçekleştirir.
 */
class BookingViewModelFactory(
    private val repository: BookingRepository
) : ViewModelProvider.Factory {

    /**
     * create: ViewModel'ın yaşam döngüsünü başlatan metot.
     * Bu metot sayesinde Dependency Injection (Manuel) sağlanmış olur.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Tip Güvenliği (Type-Safety) Kontrolü:
        // Eğer istenen sınıf (modelClass) bizim hedeflediğimiz BookingViewModel ise oluştur, değilse hata fırlat.
        if (modelClass.isAssignableFrom(BookingViewModel::class.java)) {
            // Unchecked Cast: Generic T sınıfına dönüştürme işlemi.
            // Yukarıdaki if kontrolü ile bu işlemin güvenli olduğu garantilenmiştir.
            @Suppress("UNCHECKED_CAST")
            return BookingViewModel(repository) as T
        }
        throw IllegalArgumentException("Bilinmeyen ViewModel sınıfı: İşlem reddedildi.")
    }
}