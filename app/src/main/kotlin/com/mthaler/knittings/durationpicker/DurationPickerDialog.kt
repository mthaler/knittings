package com.mthaler.knittings.durationpicker

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import com.mthaler.knittings.durationpicker.DurationPicker.OnDurationChangedListener
import com.mthaler.knittings.R
import com.mthaler.knittings.utils.TimeUtils

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
                           initialDuration: Long) : AlertDialog(context, theme), OnClickListener, OnDurationChangedListener {

    private val mTimePicker: DurationPicker

    /**
     * The callback interface used to indicate the user is done filling in
     * the time (they clicked on the 'Set' button).
     */
    interface OnTimeSetListener {

        /**
         * @param view The view associated with this listener.
         * @param duration duration in milliseconds
         */
        fun onTimeSet(view: DurationPicker, duration: Long)
    }

    /**
     * @param context Parent.
     * @param callBack How parent is notified.
     * @param hourOfDay The initial hour.
     * @param minute The initial minute.
     */
    constructor(context: Context,
                callBack: OnTimeSetListener,
                duration: Long) : this(context, 0, callBack, duration) {
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        updateTitle(0L)

        setButton(context.getText(R.string.time_set), this)
        setButton2(context.getText(R.string.dialog_button_cancel), null as OnClickListener?)
        //setIcon(android.R.drawable.ic_dialog_time);

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.duration_picker_dialog, null)
        setView(view)
        mTimePicker = view.findViewById<View>(R.id.timePicker) as DurationPicker

        // initialize state
        mTimePicker.setDuration(initialDuration)
        mTimePicker.setOnDurationChangedListener(this)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (callback != null) {
            mTimePicker.clearFocus()
            callback.onTimeSet(mTimePicker, mTimePicker.duration)
        }
    }

    override fun onDurationChanged(view: DurationPicker, duration: Long) {
        updateTitle(duration)
    }

    private fun updateTitle(duration: Long) {
        setTitle(TimeUtils.formatDuration(duration))
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putLong(DURATION, mTimePicker.duration)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val duration = savedInstanceState.getLong(DURATION)
        val seconds = duration / 1000 % 60
        val minutes = duration / (1000 * 60) % 60
        val hours = duration / (1000 * 60 * 60)
        mTimePicker.setCurrentHour(hours.toInt())
        mTimePicker.setCurrentMinute(minutes.toInt())
        mTimePicker.setCurrentSecond(seconds.toInt())
        mTimePicker.setOnDurationChangedListener(this)
        updateTitle(duration)
    }

    companion object {

        private val HOUR = "hour"
        private val MINUTE = "minute"
        private val SECONDS = "seconds"
        private val DURATION = "duration"
    }


}
