package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Context

object SaveChangesDialog {

    fun create(context: Context, onSave: () -> Unit, onDiscard: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage(context.resources.getString(R.string.save_changes_dialog_question))
        b.setPositiveButton(R.string.save_changes_dialog_button_save) { _, _ -> onSave() }
        b.setNegativeButton(R.string.save_changes_dialog_button_discard) { _, _ -> onDiscard() }
        return b.create()
    }
}