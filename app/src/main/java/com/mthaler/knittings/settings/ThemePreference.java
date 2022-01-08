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

public class ThemePreference extends Preference {

    /*private ColorShape colorShape = ColorShape.CIRCLE;
    private final int itemLayoutId = R.layout.pref_color_layout;
    private final int itemLayoutLargeId = R.layout.pref_color_layout_large;
    private final Context context;
    private String name;
    private String theme;
    private int widgetLayoutResource;*/

     public ThemePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
         super(context, attrs, defStyleAttr);
         /*this.context = context;
         initAttrs(attrs, defStyleAttr);*/
     }

  private void initAttrs(AttributeSet attrs, int defStyle) {
        /*TypedArray a = context.getTheme().obtainStyledAttributes(
            attrs, R.styleable.ThemePreference, defStyle, defStyle
        );
        PreviewSize previewSize = PreviewSize.NORMAL;
        if (previewSize == PreviewSize.NORMAL)
            this.widgetLayoutResource = this.itemLayoutId;
        else
            this.widgetLayoutResource = this.itemLayoutLargeId;*/
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        ImageView colorView = (ImageView)holder.findViewById(R.id.color_view);
        //if (colorView != null) {
            /*int color = ContextCompat.getColor(context, theme.colorId)
            ColorUtils.setColorViewValue(colorView, color, false, colorShape)
        }*/
    }

    @Override
    protected void onClick() {} {
        super.onClick();
        /*
        val activity = scanForActivity(context)
        /*val colorPicker = ColorPicker(activity)
        colorPicker.setTitle(activity!!.resources.getString(R.string.pref_color_theme))
        colorPicker.setRoundColorButton(true)
        colorPicker.setColorButtonMargin(6, 6, 6, 6)
        colorPicker.setColors(getColors(context))
        colorPicker.setOnFastChooseColorListener(object : OnFastChooseColorListener() {
            fun setOnFastChooseColorListener(position: Int, color: Int) {
                val theme = getTheme(position)
                value = theme
                activity.recreate()
            }

            fun onCancel() {}
        })
        colorPicker.show()*/
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    /*override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any) {
        val name = if (restoreValue) getPersistedString("default") else (defaultValue as String)
        val theme = getTheme(name)
        value = theme
    }*/

    /*private String getTheme() {
        return theme;
    }*/

    /*public String getFragmentTag() {
        return "color_" + getKey();
    }*/

    /*public void setFragmentTag(Theme value) {
        if (callChangeListener(value)) {
            //this.theme = value;
            persistString(value.getName());
            notifyChanged();
        }
    }*/

    /*static private Context scanForActivity(Context cont) {
        if (cont == null) return null;
        else if (cont instanceof Activity)
            return cont;
        else if(cont instanceof ContextWrapper) {
            ContextWrapper result = (ContextWrapper) cont;
            return scanForActivity(result.getBaseContext());
        } else {
            return null;
        }
    }*/
}
