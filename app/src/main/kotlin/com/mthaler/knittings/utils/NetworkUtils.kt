package com.mthaler.knittings.utils

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtils {
    /**
     * Checks if WIFI is available
     *
     * @param ctx context
     */
    fun isWifiConnected(ctx: Context): Boolean = try {
        val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo!!
        networkInfo.isConnected
    } catch (ex: Exception) {
        false
    }

    /**
     * Checks if mobile network is available
     *
     * @param ctx context
     */
    fun isMobileConnected(ctx: Context): Boolean {
        try {
            val connMgr = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!
            return networkInfo.isConnected
        } catch (ex: Exception) {
            return false
        }
    }
}