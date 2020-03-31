package com.mthaler.knittings.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.mthaler.knittings.R;

import petrov.kristiyan.colorpicker.ColorPicker;

public class ColorPreference extends Preference {

    private ColorShape colorShape = ColorShape.CIRCLE;
    private int value = 0;

    public ColorPreference(Context context) {
        super(context);
        initAttrs(null, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
    }

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ColorPreference, defStyle, defStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView colorView = (ImageView) holder.findViewById(R.id.color_view);
        if (colorView != null) {
            ColorUtils.setColorViewValue(colorView, value, false, colorShape);
        }
    }

    public void setValue(int value) {
        if (callChangeListener(value)) {
            this.value = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
        Activity activity = scanForActivity(getContext());
        ColorPicker colorPicker = new ColorPicker(activity);
        colorPicker.show();
    }

    @Override
    public void onAttached() {
        super.onAttached();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedInt(0) : (Integer) defaultValue);
    }

    public String getFragmentTag() {
        return "color_" + getKey();
    }

    public int getValue() {
        return value;
    }

    private static Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity)cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper)cont).getBaseContext());

        return null;
    }
}