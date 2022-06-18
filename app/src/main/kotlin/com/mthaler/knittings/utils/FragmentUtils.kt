package com.mthaler.knittings.utils

import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Fragment.showSnackbar(
    view: View,
    msg: String,
    length: Int,
    actionMessage: CharSequence?,
    action: (View) -> Unit
) {
    val snackbar = Snackbar.make(view, msg, length)
    if (actionMessage != null) {
        snackbar.setAction(actionMessage) {
            action(this.requireView())
        }.show()
    } else {
        snackbar.show()
    }
}