package com.mthaler.knittings.about

import android.content.Context
import java.lang.Exception

object PackageInfo {

    fun versionName(context: Context): String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (ex: Exception) {
        "Unknown"
    }

    fun versionCode(context: Context): Int = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    } catch (ex: Exception) {
        -1
    }
}