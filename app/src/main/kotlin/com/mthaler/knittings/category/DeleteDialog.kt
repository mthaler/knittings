package com.mthaler.knittings.category

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object DeleteDialog {

    fun create(context: Context, name: String, onDelete: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setMessage(context.resources.getString(R.string.delete_dialog_question, name))
        b.setPositiveButton(context.resources.getString(R.string.delete_dialog_delete_button), { diaglog, which -> onDelete() })
        b.setNegativeButton(R.string.dialog_button_cancel, { dialog, which -> })
        return b.create()
    }
}