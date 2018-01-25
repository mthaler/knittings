package com.mthaler.knittings.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {
    fun isWifiConnected(ctx: Context): Boolean {
        val connMgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected()
    }

    fun isMobileConnected(ctx: Context): Boolean {
        val connMgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return networkInfo.isConnected()
    }
}