package com.mthaler.knittings

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.DatePicker
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

class DatePickerFragment : DialogFragment() {

    private var date: Date? = null

    private fun sendResult(resultCode: Int) {
        if (targetFragment != null) {
            val i = Intent()
            i.putExtra(EXTRA_DATE, date)
            targetFragment.onActivityResult(targetRequestCode, resultCode, i)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        date = arguments.getSerializable(EXTRA_DATE) as Date

        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val v = activity.layoutInflater.inflate(R.layout.dialog_date, null)

        val datePicker = v.findViewById<DatePicker>(R.id.dialog_date_datePicker)
        datePicker.init(year, month, day) { view, year, month, day ->
            date = GregorianCalendar(year, month, day).time

            // update argument to preserve selected value on rotation
            arguments.putSerializable(EXTRA_DATE, date)
        }

        return AlertDialog.Builder(activity)
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok) { dialog, which -> sendResult(Activity.RESULT_OK) }
                .create()
    }

    companion object {
        val EXTRA_DATE = "com.mthaler.knittings.DATE"

        fun newInstance(date: Date?): DatePickerFragment {
            val args = Bundle()
            args.putSerializable(EXTRA_DATE, date)

            val fragment = DatePickerFragment()
            fragment.arguments = args

            return fragment
        }
    }
}
