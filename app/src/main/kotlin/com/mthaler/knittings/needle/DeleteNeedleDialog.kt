package com.mthaler.knittings.needle

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object DeleteNeedleDialog {

    fun create(context: Context, onDelete: () -> Unit): AlertDialog {
        // create the dialog
        val b = AlertDialog.Builder(context)
        b.setTitle(R.string.delete_needle_dialog_title)
        b.setMessage(R.string.delete_needle_dialog_question)
        b.setPositiveButton(R.string.delete_needle_dialog_delete_button, { diaglog, which -> onDelete() })
        b.setNegativeButton(R.string.dialog_button_cancel, {dialog, which -> })
        return b.create()
    }
}