package com.mthaler.knittings.category

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object SaveChangesDialog {

    fun create(context: Context, onSave: () -> Unit, onDiscard: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setTitle(R.string.save_changes_category_dialog_title)
        b.setMessage(context.resources.getString(R.string.save_changes_category_dialog_question))
        b.setPositiveButton(R.string.dialog_button_yes, { diaglog, which -> onSave() })
        b.setNegativeButton(R.string.dialog_button_no, { dialog, which -> onDiscard() })
        b.setNeutralButton(R.string.dialog_button_cancel) { dialog, which -> }
        return b.create()
    }
}