package com.zoiper.zdk.android.demokt.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager

/**
 *NetworkChangeReceiver
 *
 *@since 28/03/2019
 */
class NetworkChangeReceiver(private val zdkContext: com.zoiper.zdk.Context) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        zdkContext.networkChanged()
    }

    companion object {
        fun intentFilter() : IntentFilter{
            val intentFilter = IntentFilter()
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            return intentFilter
        }
    }
}