package com.mthaler.knittings.durationpicker

import java.util.Calendar

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.Window

import com.mthaler.knittings.durationpicker.DurationPicker.OnTimeChangedListener
import com.mthaler.knittings.R

/**
 * A dialog that prompts the user for the time of day using a [DurationPicker].
 *
 *  * @param context Parent.
 * @param theme the theme to apply to this dialog
 * @param callBack How parent is notified.
 * @param hourOfDay The initial hour.
 * @param minute The initial minute.
 * @param is24HourView Whether this is a 24 hour view, or AM/PM.
 */
class DurationPickerDialog(context: Context,
                           theme: Int,
                           private val callback: OnTimeSetListener,
                           internal var mInitialHourOfDay: Int,
                           internal var mInitialMinute: Int,
                           internal var mInitialSeconds: Int,
                           internal var mIs24HourView: Boolean) : AlertDialog(context, theme), OnClickListener, OnTimeChangedListener {

    private val mTimePicker: DurationPicker
    private val mCalendar: Calendar
    private val mDateFormat: java.text.DateFormat

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    interface OnTimeSetListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The hour that was set.
         * @param minute The minute that was set.
         */
        fun onTimeSet(view: DurationPicker, hourOfDay: Int, minute: Int, seconds: Int)
    }

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     * @param is24HourView Whether this is a 24 hour view, or AM/PM.
     */
    constructor(context: Context,
                callBack: OnTimeSetListener,
                hourOfDay: Int, minute: Int, seconds: Int, is24HourView: Boolean) : this(context, 0,
            callBack, hourOfDay, minute, seconds, is24HourView) {
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        mDateFormat = DateFormat.getTimeFormat(context)
        mCalendar = Calendar.getInstance()
        updateTitle(mInitialHourOfDay, mInitialMinute, mInitialSeconds)

        setButton(context.getText(R.string.time_set), this)
        setButton2(context.getText(R.string.cancel), null as OnClickListener?)
        //setIcon(android.R.drawable.ic_dialog_time);

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.duration_picker_dialog, null)
        setView(view)
        mTimePicker = view.findViewById<View>(R.id.timePicker) as DurationPicker

        // initialize state
        mTimePicker.currentHour = mInitialHourOfDay
        mTimePicker.currentMinute = mInitialMinute
        mTimePicker.setCurrentSecond(mInitialSeconds)
        mTimePicker.setOnTimeChangedListener(this)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (callback != null) {
            mTimePicker.clearFocus()
            callback.onTimeSet(mTimePicker, mTimePicker.currentHour!!,
                    mTimePicker.currentMinute!!, mTimePicker.currentSeconds!!)
        }
    }

    override fun onTimeChanged(view: DurationPicker, hourOfDay: Int, minute: Int, seconds: Int) {
        updateTitle(hourOfDay, minute, seconds)
    }

    fun updateTime(hourOfDay: Int, minutOfHour: Int, seconds: Int) {
        mTimePicker.currentHour = hourOfDay
        mTimePicker.currentMinute = minutOfHour
        mTimePicker.setCurrentSecond(seconds)
    }

    private fun updateTitle(hour: Int, minute: Int, seconds: Int) {
        val sHour = String.format("%02d", hour)
        val sMin = String.format("%02d", minute)
        val sSec = String.format("%02d", seconds)
        setTitle("$sHour:$sMin:$sSec")
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(HOUR, mTimePicker.currentHour!!)
        state.putInt(MINUTE, mTimePicker.currentMinute!!)
        state.putInt(SECONDS, mTimePicker.currentSeconds!!)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(HOUR)
        val minute = savedInstanceState.getInt(MINUTE)
        val seconds = savedInstanceState.getInt(SECONDS)
        mTimePicker.currentHour = hour
        mTimePicker.currentMinute = minute
        mTimePicker.setCurrentSecond(seconds)
        mTimePicker.setOnTimeChangedListener(this)
        updateTitle(hour, minute, seconds)
    }

    companion object {

        private val HOUR = "hour"
        private val MINUTE = "minute"
        private val SECONDS = "seconds"
        private val IS_24_HOUR = "is24hour"
    }


}
