package com.mthaler.knittings.whatsnew

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.mthaler.knittings.R

object WhatsNewDialog {

    fun show(activity: Activity) {
        @SuppressLint("InflateParams")
        val layoutInflater = activity.layoutInflater
        val v = layoutInflater.inflate(R.layout.dialog_whats_new, null)
        val versionHistory = v.findViewById<TextView>(R.id.version_history)
        val text = HtmlCompat.fromHtml(activity.getString(R.string.whats_new), 0)
        versionHistory.text = text
        val b = AlertDialog.Builder(activity)
        b.setView(v)
        b.setPositiveButton(android.R.string.ok) { diag, i -> diag.dismiss() }
        b.create().show()
    }
}