package com.mthaler.knittings.settings;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import com.mthaler.knittings.R;
import petrov.kristiyan.colorpicker.ColorPicker;

public class ThemePreference extends Preference {

    private ColorShape colorShape = ColorShape.CIRCLE;
    private Theme theme = Theme.Companion.getDefault();
    private int itemLayoutId = R.layout.pref_color_layout;
    private int itemLayoutLargeId = R.layout.pref_color_layout_large;

    public ThemePreference(Context context) {
        super(context);
        initAttrs(null, 0);
    }

    public ThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
    }

    public ThemePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
    }

    private void initAttrs(AttributeSet attrs, int defStyle) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.ThemePreference, defStyle, defStyle);
        PreviewSize previewSize = PreviewSize.NORMAL;
        setWidgetLayoutResource(previewSize == PreviewSize.NORMAL ? itemLayoutId : itemLayoutLargeId);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView colorView = (ImageView) holder.findViewById(R.id.color_view);
        if (colorView != null) {
            int color = ContextCompat.getColor(getContext(), theme.getColorId());
            ColorUtils.setColorViewValue(colorView, color, false, colorShape);
        }
    }

    public void setValue(Theme value) {
        if (callChangeListener(value)) {
            this.theme = value;
            persistString(value.getName());
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
        Activity activity = scanForActivity(getContext());
        ColorPicker colorPicker = new ColorPicker(activity);
        colorPicker.setColorButtonDrawable(petrov.kristiyan.colorpicker.R.drawable.round_button);
        colorPicker.setColorButtonMargin(6, 6,6, 6);
        colorPicker.setColors(Theme.Companion.getColors(getContext()));
        colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
            @Override
            public void setOnFastChooseColorListener(int position, int color) {
                Theme theme = Theme.Companion.getTheme(position);
                setValue(theme);
            }

            @Override
            public void onCancel() {

            }
        });
        colorPicker.show();
    }

    @Override
    public void onAttached() {
        super.onAttached();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String name = restoreValue ? getPersistedString("default") : (String) defaultValue;
        Theme theme = Theme.Companion.getTheme(name);
        setValue(theme);
    }

    public String getFragmentTag() {
        return "color_" + getKey();
    }

    public Theme getValue() {
        return theme;
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
