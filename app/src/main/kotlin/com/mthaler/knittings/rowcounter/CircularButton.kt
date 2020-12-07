package com.mthaler.knittings.rowcounter

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.mthaler.knittings.R

class CircularButton@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    init {
        isFocusable = true
        scaleType = ScaleType.CENTER_INSIDE
        isClickable = true

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularButton)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // do something
    }
}