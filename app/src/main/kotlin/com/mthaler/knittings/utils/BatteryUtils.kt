package com.mthaler.knittings.utils

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter

object BatteryUtils {

    /**
     * Returns the current battery level or NaN if there is some error
     *
     * @arg ctx context
     */
    fun getBatteryLevel(ctx: Context): Float  {
        val batteryIntent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale === -1) {
            return Float.NaN
        } else {
            return level.toFloat() / scale as Float * 100.0f
        }

    }
}