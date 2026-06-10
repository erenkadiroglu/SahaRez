package com.erenkadiroglu.saharez.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * NetworkMonitor: İşletim sistemi seviyesindeki ağ bağlantı durumunu dinleyen araç sınıfı.
 * Coroutine StateFlow yapısı kullanılarak, bağlantı değişiklikleri uygulama geneline
 * reaktif (Reactive) bir şekilde yayınlanır (Publish/Subscribe mantığı).
 */
class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Encapsulation (Kapsülleme): Dışarıya sadece okunabilir StateFlow açılmıştır.
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    init {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Ağ bağlantısı kurulduğunda tetiklenen asenkron callback (Auto-Sync tetikleyicisi)
                _isConnected.value = true
            }

            override fun onLost(network: Network) {
                // Bağlantı koptuğu an state güncellenir
                _isConnected.value = false
            }
        })
    }
}