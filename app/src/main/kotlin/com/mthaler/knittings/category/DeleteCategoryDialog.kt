package com.mthaler.knittings.category

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category

/**
 * Displays a dialog that asks the user to confirm that the knitting should be deleted
 */
object DeleteCategoryDialog {

    fun create(context: Context, category: Category, onDelete: () -> Unit): AlertDialog {
        // create the dialog
        val b = AlertDialog.Builder(context)
        b.setTitle(R.string.delete_category_dialog_title)
        b.setMessage(context.resources.getString(R.string.delete_category_dialog_question))
        b.setPositiveButton(context.resources.getString(R.string.delete_category_dialog_delete_button), { diaglog, which -> onDelete() })
        b.setNegativeButton(R.string.dialog_button_cancel, { dialog, which -> })
        return b.create()
    }
}