package com.mthaler.knittings.utils

import android.app.DatePickerDialog
import android.content.Context
import android.widget.DatePicker
import java.util.*

object DatePickerUtils {

    fun create(context: Context, date: Date, onDateSet: (view: DatePicker?, date: Date) -> Unit): DatePickerDialog {
        val c = Calendar.getInstance()
        c.time = date
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        return DatePickerDialog(context, { view, y, m, d ->
            val c1 = Calendar.getInstance()
            c1.timeInMillis = 0
            c1.set(y, m, d)
            onDateSet(view, c1.time)
        }, year, month, day)
    }
}