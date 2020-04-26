package com.mthaler.knittings

import android.app.AlertDialog
import android.content.Context

object DiscardChangesDialog {

    fun create(context: Context, onDiscard: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage(context.resources.getString(R.string.discard_changes_dialog_question))
        b.setPositiveButton(R.string.discard_changes_dialog_button_discard, { diaglog, which -> onDiscard() })
        b.setNegativeButton(R.string.dialog_button_cancel, { dialog, which ->  })
        return b.create()
    }
}