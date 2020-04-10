package com.mthaler.knittings.durationpicker

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.NumberPicker.Formatter
import com.mthaler.knittings.R

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode.
 *
 * The hour, each minute digit, each seconds digit, and AM/PM (if applicable) can be conrolled by
 * vertical spinners.
 *
 * The hour can be entered by keyboard input.  Entering in two digit hours
 * can be accomplished by hitting two digits within a timeout of about a
 * second (e.g. '1' then '2' to select 12).
 *
 * The minutes can be entered by entering single digits.
 * The seconds can be entered by entering single digits.
 *
 * Under AM/PM mode, the user can hit 'a', 'A", 'p' or 'P' to pick.
 *
 * For a dialog using this view, see [android.app.TimePickerDialog].
 */
class DurationPicker @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {

    // state
    private var currentHours = 0 // 0-9999
    private var currentMinutes = 0 // 0-59
    private var currentSeconds = 0 // 0-59

    // ui components
    private val hourPicker: NumberPicker
    private val minutePicker: NumberPicker
    private val secondPicker: NumberPicker

    // callbacks
    private var onDurationChangedListener: OnDurationChangedListener? = null

    /**
     * Returns the duration in milliseconds
     *
     * @return duration in milliseconds
     */
    val duration: Long
        get() = 1000L * (currentSeconds + 60 * currentMinutes + 3600 * currentHours)

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    interface OnDurationChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param duration duration in milliseconds.
         */
        fun onDurationChanged(view: DurationPicker, duration: Long)
    }

    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.duration_picker_widget, this, true)

        // hour
        hourPicker = findViewById(R.id.hour)
        hourPicker.minValue = 0
        hourPicker.maxValue = 9999
        hourPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            currentHours = newVal
            onTimeChanged()
        }

        // digits of minute
        minutePicker = findViewById(R.id.minute)
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.setFormatter(TWO_DIGIT_FORMATTER)
        minutePicker.setOnValueChangedListener { spinner, oldVal, newVal ->
            currentMinutes = newVal
            onTimeChanged()
        }

        // digits of seconds
        secondPicker = findViewById(R.id.seconds)
        secondPicker.minValue = 0
        secondPicker.maxValue = 59
        secondPicker.setFormatter(TWO_DIGIT_FORMATTER)
        secondPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            currentSeconds = newVal
            onTimeChanged()
        }

        // initialize to current time
        setOnDurationChangedListener(NO_OP_CHANGE_LISTENER)
    }

    /**
     * Used to save / restore state of time picker
     */
    private class SavedState : BaseSavedState {

        val duration: Long

        constructor(superState: Parcelable, duration: Long) : super(superState) {
            this.duration = duration
        }

        constructor(`in`: Parcel) : super(`in`) {
            duration = `in`.readLong()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeLong(duration)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, duration)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        setDuration(ss.duration)
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onDurationChangedListener the callback, should not be null.
     */
    fun setOnDurationChangedListener(onDurationChangedListener: OnDurationChangedListener) {
        this.onDurationChangedListener = onDurationChangedListener
    }

    fun setDuration(duration: Long) {
        val seconds = duration / 1000 % 60
        val minutes = duration / (1000 * 60) % 60
        val hours = duration / (1000 * 60 * 60)
        setCurrentHour(hours.toInt())
        setCurrentMinute(minutes.toInt())
        setCurrentSecond(seconds.toInt())
    }

    /**
     * Set the current hour.
     */
    fun setCurrentHour(currentHour: Int) {
        this.currentHours = currentHour
        updateHourDisplay()
    }

    /**
     * Set the current minute (0-59).
     */
    fun setCurrentMinute(currentMinute: Int) {
        this.currentMinutes = currentMinute
        updateMinuteDisplay()
    }

    /**
     * Set the current second (0-59).
     */
    fun setCurrentSecond(currentSecond: Int) {
        this.currentSeconds = currentSecond
        updateSecondsDisplay()
    }

    override fun getBaseline(): Int = hourPicker.baseline

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private fun updateHourDisplay() {
        val currentHour = currentHours
        hourPicker.value = currentHour
        onTimeChanged()
    }

    private fun onTimeChanged() {
        onDurationChangedListener?.onDurationChanged(this, duration)
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private fun updateMinuteDisplay() {
        minutePicker.value = currentMinutes
        onDurationChangedListener?.onDurationChanged(this, duration)
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private fun updateSecondsDisplay() {
        secondPicker.value = currentSeconds
        onDurationChangedListener?.onDurationChanged(this, duration)
    }

    companion object {

        /**
         * A no-op callback used in the constructor to avoid null checks
         * later in the code.
         */
        private val NO_OP_CHANGE_LISTENER = object : OnDurationChangedListener {
            override fun onDurationChanged(view: DurationPicker, duration: Long) {}
        }

        val TWO_DIGIT_FORMATTER: Formatter = Formatter { value ->
            // TODO Auto-generated method stub
            String.format("%02d", value)
        }
    }
}

