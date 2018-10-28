package com.mthaler.knittings.about

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.R
import org.jetbrains.anko.find

object AboutDialog {

    /**
     * Shows the about dialog that displays the app name, version and some additional information
     *
     * @param activity activity from which the about dialog is shown
     */
    fun show(activity: Activity) {
        @SuppressLint("InflateParams")
        val layoutInflater = activity.layoutInflater
        val v = layoutInflater.inflate(R.layout.dialog_about, null)
        val appName = v.find<TextView>(R.id.about_app_name)
        appName.text = (appName.text.toString() + " " + BuildConfig.VERSION_NAME)
        val b = AlertDialog.Builder(activity)
        b.setView(v)
        b.setPositiveButton(android.R.string.ok ) { diag, i -> diag.dismiss()}
        b.create().show()
    }
}