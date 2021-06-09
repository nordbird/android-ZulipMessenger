package ru.nordbird.tfsmessenger.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable
import ru.nordbird.tfsmessenger.di.scope.AppScope
import javax.inject.Inject
import javax.inject.Singleton

@AppScope
class RxConnectionObservable @Inject constructor(
    context: Context
) {

    val networkState: Observable<Boolean> get() = relay.distinctUntilChanged()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val networkRequestBuilder: NetworkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .build()
    private val relay: BehaviorRelay<Boolean> = BehaviorRelay.createDefault(isConnected())

    init {
        connectivityManager.registerNetworkCallback(networkRequestBuilder, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                relay.accept(isConnected())
            }

            override fun onLost(network: Network) {
                relay.accept(isConnected())
            }
        })
    }

    private fun isConnected(): Boolean {
        for (network in connectivityManager.allNetworks) {
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasCapability(NET_CAPABILITY_INTERNET) == true) {
                return true
            }
        }
        return false
    }
}