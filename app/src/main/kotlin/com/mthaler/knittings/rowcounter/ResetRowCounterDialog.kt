package com.mthaler.knittings.rowcounter

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object ResetRowCounterDialog {

    fun create(context: Context, onReset: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage(R.string.delete_row_counter_dialog_message)
        b.setPositiveButton(R.string.delete_row_counter_dialog_reset) { _, _ -> onReset() }
        b.setNegativeButton(R.string.dialog_button_cancel) { _, _ -> }
        return b.create()
    }
}