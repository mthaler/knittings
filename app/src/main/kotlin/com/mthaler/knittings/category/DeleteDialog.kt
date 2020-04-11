package com.mthaler.knittings.category

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R

object DeleteDialog {

    fun create(context: Context, categoryName: String, onDelete: () -> Unit): AlertDialog {
        val b = AlertDialog.Builder(context)
        b.setTitle(R.string.delete_category_dialog_title)
        b.setMessage(context.resources.getString(R.string.delete_category_dialog_question, categoryName))
        b.setPositiveButton(context.resources.getString(R.string.delete_category_dialog_delete_button), { diaglog, which -> onDelete() })
        b.setNegativeButton(R.string.dialog_button_cancel, { dialog, which -> })
        return b.create()
    }
}