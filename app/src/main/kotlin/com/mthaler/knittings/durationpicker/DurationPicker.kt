package com.mthaler.knittings.durationpicker

import java.util.Calendar
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
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
    private var mCurrentHours = 0 // 0-23
    private var mCurrentMinutes = 0 // 0-59
    private var mCurrentSeconds = 0 // 0-59

    // ui components
    private val mHourPicker: NumberPicker
    private val mMinutePicker: NumberPicker
    private val mSecondPicker: NumberPicker

    // callbacks
    private var mOnDurationChangedListener: OnDurationChangedListener? = null

    /**
     * Returns the duration in milliseconds
     *
     * @return duration in milliseconds
     */
    val duration: Long
        get() = 1000L * (mCurrentSeconds + 60 * mCurrentMinutes + 3600 * mCurrentHours)

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
        inflater.inflate(R.layout.duration_picker_widget,
                this, // we are the parent
                true)

        // hour
        mHourPicker = findViewById(R.id.hour)
        mHourPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            // TODO Auto-generated method stub
            mCurrentHours = newVal
            onTimeChanged()
        }

        // digits of minute
        mMinutePicker = findViewById(R.id.minute)
        mMinutePicker.minValue = 0
        mMinutePicker.maxValue = 59
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER)
        mMinutePicker.setOnValueChangedListener { spinner, oldVal, newVal ->
            mCurrentMinutes = newVal
            onTimeChanged()
        }

        // digits of seconds
        mSecondPicker = findViewById(R.id.seconds)
        mSecondPicker.minValue = 0
        mSecondPicker.maxValue = 59
        mSecondPicker.setFormatter(TWO_DIGIT_FORMATTER)
        mSecondPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            mCurrentSeconds = newVal
            onTimeChanged()
        }

        // am/pm
        //mAmPmButton = (Button) findViewById(R.id.amPm);

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges()

        // initialize to current time
        val cal = Calendar.getInstance()
        setOnDurationChangedListener(NO_OP_CHANGE_LISTENER)

        // by default we're not in 24 hour mode
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY))
        setCurrentMinute(cal.get(Calendar.MINUTE))
        setCurrentSecond(cal.get(Calendar.SECOND))

        if (!isEnabled) {
            isEnabled = false
        }
    }

    /**
     * Used to save / restore state of time picker
     */
    private class SavedState : View.BaseSavedState {

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

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls<SavedState>(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, duration!!)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.getSuperState())
        setDuration(ss.duration)
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onDurationChangedListener the callback, should not be null.
     */
    fun setOnDurationChangedListener(onDurationChangedListener: OnDurationChangedListener) {
        mOnDurationChangedListener = onDurationChangedListener
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
    fun setCurrentHour(currentHour: Int?) {
        this.mCurrentHours = currentHour!!
        updateHourDisplay()
    }

    /**
     * Set the current minute (0-59).
     */
    fun setCurrentMinute(currentMinute: Int?) {
        this.mCurrentMinutes = currentMinute!!
        updateMinuteDisplay()
    }

    /**
     * Set the current second (0-59).
     */
    fun setCurrentSecond(currentSecond: Int?) {
        this.mCurrentSeconds = currentSecond!!
        updateSecondsDisplay()
    }

    override fun getBaseline(): Int {
        return mHourPicker.baseline
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private fun updateHourDisplay() {
        val currentHour = mCurrentHours
        mHourPicker.value = currentHour
        onTimeChanged()
    }

    private fun configurePickerRanges() {
        mHourPicker.minValue = 0
        mHourPicker.maxValue = 23
        mHourPicker.setFormatter(TWO_DIGIT_FORMATTER)
    }

    private fun onTimeChanged() {
        mOnDurationChangedListener!!.onDurationChanged(this, duration!!)
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private fun updateMinuteDisplay() {
        mMinutePicker.value = mCurrentMinutes
        mOnDurationChangedListener!!.onDurationChanged(this, duration!!)
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private fun updateSecondsDisplay() {
        mSecondPicker.value = mCurrentSeconds
        mOnDurationChangedListener!!.onDurationChanged(this, duration!!)
    }

    companion object {

        /**
         * A no-op callback used in the constructor to avoid null checks
         * later in the code.
         */
        private val NO_OP_CHANGE_LISTENER = object : OnDurationChangedListener {
            override fun onDurationChanged(view: DurationPicker, duration: Long) {}
        }

        val TWO_DIGIT_FORMATTER: NumberPicker.Formatter = Formatter { value ->
            // TODO Auto-generated method stub
            String.format("%02d", value)
        }
    }
}

