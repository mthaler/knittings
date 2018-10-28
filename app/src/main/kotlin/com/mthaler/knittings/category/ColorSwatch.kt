package com.mthaler.knittings.category

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.mthaler.knittings.R

/**
 * The ColorSwatch class implements a view that displays a color using a filled circle
 *
 * @param context Context
 * @param attrs attribute set passed to the view if it is instantiated from XML
 */
class ColorSwatch(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context): this(context, null)

    private var _color = DEFAULT_COLOR
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var size = 0

    var color: Int
        get() = _color
        set(value) {
            _color = value
            invalidate()
        }

    init {
        paint.isAntiAlias = true
        setupAttributes(attrs)
    }

    /**
     * Handles the attributes passed if the color swatch is instantiated from XML
     */
    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ColorSwatch, 0, 0)
        color = typedArray.getColor(R.styleable.ColorSwatch_color, DEFAULT_COLOR)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // draw a filled circle with the given color
        paint.color = _color
        paint.style = Paint.Style.FILL
        val radius = size / 2f
        canvas.drawCircle(size / 2f, size / 2f, radius, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // determine the size of our color swatch
        size = Math.min(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    companion object {
        private const val DEFAULT_COLOR = Color.RED
    }
}