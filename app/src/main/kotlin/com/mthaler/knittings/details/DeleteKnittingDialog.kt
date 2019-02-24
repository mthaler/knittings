package com.mthaler.knittings.details

import android.app.AlertDialog
import android.content.Context
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Knitting

/**
 * Displays a dialog that asks the user to confirm that the knitting should be deleted
 */
object DeleteKnittingDialog {

    fun create(context: Context, knitting: Knitting, onDelete: () -> Unit): AlertDialog {
        // create the dialog
        val b = AlertDialog.Builder(context)
        b.setTitle(R.string.delete_knitting_dialog_title)
        b.setMessage(context.resources.getString(R.string.delete_knitting_dialog_question, knitting.title))
        b.setPositiveButton(R.string.delete_knitting_dialog_delete_button, { diaglog, which -> onDelete() })
        b.setNegativeButton(R.string.dialog_button_cancel, {dialog, which -> })
        return b.create()
    }
}