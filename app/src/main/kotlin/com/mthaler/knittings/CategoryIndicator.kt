package com.mthaler.knittings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

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

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ColorSwatch, 0, 0)
        color = typedArray.getColor(R.styleable.ColorSwatch_color, DEFAULT_COLOR)
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)
        paint.color = _color
        paint.style = Paint.Style.FILL
        canvas.drawRect(0.toFloat(), 0.toFloat(), _width.toFloat(), _height.toFloat(), paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        _width = measuredWidth
        _height = measuredHeight
        setMeasuredDimension(_width, _height)
    }

    companion object {
        private const val DEFAULT_COLOR = Color.RED
    }
}