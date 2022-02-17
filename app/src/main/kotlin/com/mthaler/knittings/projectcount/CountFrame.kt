package com.mthaler.knittings.projectcount

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.mthaler.knittings.R

class CountFrame @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.STROKE
    }

    private var rect = RectF(0f, 0f, 1f, 1f)

    private var rx = 150f
    private var ry = 150f

    init {
        setWillNotDraw(false)
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CountFrame)
        val frameWidth= typedArray.getDimension(R.styleable.CountFrame_frameWidth,40f)
        paint.strokeWidth = frameWidth
        rx = typedArray.getDimension(R.styleable.CountFrame_frameRadiusX, 150f)
        ry = typedArray.getDimension(R.styleable.CountFrame_frameRadiusY, 150f)
        val frameColor = typedArray.getColor(R.styleable.CountFrame_frameColor, Color.BLACK)
        paint.color = frameColor
        typedArray.recycle()
    }

    /**
     * This is called during layout when the size of this view has changed. If
     * the view was just added to the view hierarchy, it is called with the old
     * values of 0. The code determines the drawing bounds for the custom view.
     *
     * @param width    Current width of this view.
     * @param height    Current height of this view.
     * @param oldWidth Old width of this view.
     * @param oldHeight Old height of this view.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        rect = RectF(0.025f * width, 0.05f * height, 0.975f * width, 0.95f * height)
    }

    /**
     * Renders view content: an outer circle to serve as the "dial",
     * and a smaller black circle to server as the indicator.
     * The position of the indicator is based on fanSpeed.
     *
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw frame
        canvas.drawRoundRect(rect, rx, ry, paint)
    }
}