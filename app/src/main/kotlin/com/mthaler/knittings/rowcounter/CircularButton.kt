package com.mthaler.knittings.rowcounter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.ColorUtils
import com.mthaler.knittings.R
import kotlin.math.min

class CircularButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.FILL
    }

    private var radius = 0.0f // Radius of the circle.
    private var color = Color.BLACK

    init {
        isFocusable = true
        scaleType = ScaleType.CENTER_INSIDE
        isClickable = true

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularButton)
        color = typedArray.getColor(R.styleable.CircularButton_backgroundColor,0)
        paint.color = color
        typedArray.recycle()
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        if (pressed) {
            paint.color = ColorUtils.blendARGB(color, Color.BLACK, 0.1f)
        } else {
            paint.color = color
        }
        invalidate()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        // Calculate the radius from the smaller of the width and height.
        radius = (min(width, height) / 2.0).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        super.onDraw(canvas)
    }
}