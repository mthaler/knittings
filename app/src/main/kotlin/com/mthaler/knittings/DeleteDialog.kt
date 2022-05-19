package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Context

object DeleteDialog {

    fun create(context: Context, name: String, onDelete: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage(context.resources.getString(R.string.delete_dialog_question, name))
        b.setPositiveButton(context.resources.getString(R.string.delete_dialog_delete_button)) { _, _ -> onDelete() }
        b.setNegativeButton(R.string.dialog_button_cancel) { _, _ -> }
        return b.create()
    }
}