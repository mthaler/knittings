package com.mthaler.knittings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * CategoryIndicator is used to display the category of a knitting in the knitting list
 * It shows the category color as a small vertical bar
 *
 * @param context context
 * @param attrs attribute set passed when instantiating the category indicator from XML
 */
class CategoryIndicator(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    constructor(context: Context): this(context, null)

    private var _color = DEFAULT_COLOR
    private val paint = Paint()
    private var _width = 0
    private var _height = 0

    var color: Int
        get() = _color
        set(value) {
            _color = value
            invalidate()
        }

    init {
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
        // draw a filled rectangle with the given color
        paint.color = _color
        paint.style = Paint.Style.FILL
        canvas.drawRect(0.toFloat(), 0.toFloat(), _width.toFloat(), _height.toFloat(), paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        // determine the width and height of the rectangle
        _width = measuredWidth
        _height = measuredHeight
        setMeasuredDimension(_width, _height)
    }

    companion object {
        private const val DEFAULT_COLOR = Color.RED
    }
}