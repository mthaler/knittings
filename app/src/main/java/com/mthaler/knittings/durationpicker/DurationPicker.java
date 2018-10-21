/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2013 Ivan Kovac navratnanos@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mthaler.knittings.durationpicker;

import java.util.Calendar;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.Formatter;

import com.mthaler.knittings.R;

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
 * For a dialog using this view, see {@link android.app.TimePickerDialog}.
 */
public class DurationPicker extends FrameLayout {

    /**
     * A no-op callback used in the constructor to avoid null checks
     * later in the code.
     */
    private static final OnDurationChangedListener NO_OP_CHANGE_LISTENER = new OnDurationChangedListener() {
        public void onDurationChanged(DurationPicker view, int hourOfDay, int minute, int seconds) {
        }
    };

    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
            new Formatter() {

                @Override
                public String format(int value) {
                    // TODO Auto-generated method stub
                    return String.format("%02d", value);
                }
            };

    // state
    private int mCurrentHours = 0; // 0-23
    private int mCurrentMinutes = 0; // 0-59
    private int mCurrentSeconds = 0; // 0-59

    // ui components
    private final NumberPicker mHourPicker;
    private final NumberPicker mMinutePicker;
    private final NumberPicker mSecondPicker;

    // callbacks
    private OnDurationChangedListener mOnDurationChangedListener;

    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    public interface OnDurationChangedListener {

        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param seconds The current second.
         */
        void onDurationChanged(DurationPicker view, int hourOfDay, int minute, int seconds);
    }

    public DurationPicker(Context context) {
        this(context, null);
    }

    public DurationPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DurationPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.duration_picker_widget,
                this, // we are the parent
                true);

        // hour
        mHourPicker = (NumberPicker) findViewById(R.id.hour);
        mHourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                mCurrentHours = newVal;
                onTimeChanged();
            }
        });

        // digits of minute
        mMinutePicker = (NumberPicker) findViewById(R.id.minute);
        mMinutePicker.setMinValue(0);
        mMinutePicker.setMaxValue(11);
        mMinutePicker.setFormatter(TWO_DIGIT_FORMATTER);
        mMinutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker spinner, int oldVal, int newVal) {
                mCurrentMinutes = newVal;
                onTimeChanged();
            }
        });

        // digits of seconds
        mSecondPicker = (NumberPicker) findViewById(R.id.seconds);
        mSecondPicker.setMinValue(0);
        mSecondPicker.setMaxValue(11);
        mSecondPicker.setFormatter( TWO_DIGIT_FORMATTER);
        mSecondPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mCurrentSeconds = newVal;
                onTimeChanged();

            }
        });

        // am/pm
        //mAmPmButton = (Button) findViewById(R.id.amPm);

        // now that the hour/minute picker objects have been initialized, set
        // the hour range properly based on the 12/24 hour display mode.
        configurePickerRanges();

        // initialize to current time
        Calendar cal = Calendar.getInstance();
        setOnDurationChangedListener(NO_OP_CHANGE_LISTENER);

        // by default we're not in 24 hour mode
        setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        setCurrentMinute(cal.get(Calendar.MINUTE));
        setCurrentSecond(cal.get(Calendar.SECOND));

        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    /**
     * Used to save / restore state of time picker
     */
    private static class SavedState extends BaseSavedState {

        private final int mHour;
        private final int mMinute;

        private SavedState(Parcelable superState, int hour, int minute) {
            super(superState);
            mHour = hour;
            mMinute = minute;
        }

        private SavedState(Parcel in) {
            super(in);
            mHour = in.readInt();
            mMinute = in.readInt();
        }

        public int getHour() {
            return mHour;
        }

        public int getMinute() {
            return mMinute;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mHour);
            dest.writeInt(mMinute);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mCurrentHours, mCurrentMinutes);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setCurrentHour(ss.getHour());
        setCurrentMinute(ss.getMinute());
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     * @param onDurationChangedListener the callback, should not be null.
     */
    public void setOnDurationChangedListener(OnDurationChangedListener onDurationChangedListener) {
        mOnDurationChangedListener = onDurationChangedListener;
    }

    public Long getDuration() {
        return 1000L * (mCurrentSeconds + 60 * mCurrentMinutes + 3600 * mCurrentHours);
    }

    /**
     * @return The current hour (0-23).
     */
    public Integer getCurrentHour() {
        return mCurrentHours;
    }

    /**
     * Set the current hour.
     */
    public void setCurrentHour(Integer currentHour) {
        this.mCurrentHours = currentHour;
        updateHourDisplay();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentMinute() {
        return mCurrentMinutes;
    }

    /**
     * Set the current minute (0-59).
     */
    public void setCurrentMinute(Integer currentMinute) {
        this.mCurrentMinutes = currentMinute;
        updateMinuteDisplay();
    }

    /**
     * @return The current minute.
     */
    public Integer getCurrentSeconds() {
        return mCurrentSeconds;
    }

    /**
     * Set the current second (0-59).
     */
    public void setCurrentSecond(Integer currentSecond) {
        this.mCurrentSeconds = currentSecond;
        updateSecondsDisplay();
    }

    @Override
    public int getBaseline() {
        return mHourPicker.getBaseline();
    }

    /**
     * Set the state of the spinners appropriate to the current hour.
     */
    private void updateHourDisplay() {
        int currentHour = mCurrentHours;
        mHourPicker.setValue(currentHour);
        onTimeChanged();
    }

    private void configurePickerRanges() {
        mHourPicker.setMinValue(0);
        mHourPicker.setMaxValue(23);
        mHourPicker.setFormatter(TWO_DIGIT_FORMATTER);
    }

    private void onTimeChanged() {
        mOnDurationChangedListener.onDurationChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }

    /**
     * Set the state of the spinners appropriate to the current minute.
     */
    private void updateMinuteDisplay() {
        mMinutePicker.setValue(mCurrentMinutes);
        mOnDurationChangedListener.onDurationChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }

    /**
     * Set the state of the spinners appropriate to the current second.
     */
    private void updateSecondsDisplay() {
        mSecondPicker.setValue(mCurrentSeconds);
        mOnDurationChangedListener.onDurationChanged(this, getCurrentHour(), getCurrentMinute(), getCurrentSeconds());
    }
}

