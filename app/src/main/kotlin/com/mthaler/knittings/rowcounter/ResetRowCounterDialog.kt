package com.mthaler.knittings.rowcounter

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object ResetRowCounterDialog {

    fun create(context: Context, onReset: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage("Do you really want to reset row counter?")
        b.setPositiveButton("Reset") { diaglog, which -> onReset() }
        b.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> }
        return b.create()
    }
}