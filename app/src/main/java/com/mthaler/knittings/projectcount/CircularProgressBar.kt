package com.mthaler.knittings.projectcount

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import com.mthaler.knittings.R
import kotlin.math.min

class CircularProgressBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Paint styles used for rendering are initialized here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private var oval = RectF(0f, 0f, 1f, 1f)

    private val ringColor: Int

    var progress: Float = 0f
    set(value) {
        field = value
        invalidate()
    }

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressBar)
        val ringWidth = typedArray.getDimension(R.styleable.CircularProgressBar_ringWidth, 25f)
        paint.strokeWidth = ringWidth
        ringColor = typedArray.getColor(R.styleable.CircularProgressBar_ringColor,0)
        progress = typedArray.getFloat(R.styleable.CircularProgressBar_progress, 0f)
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
        // Calculate the radius from the smaller of the width and height.
        val radius = (min(width, height) / 2.0 * 0.9).toFloat()
        val cx = (width / 2.0).toFloat()
        val cy = (height / 2.0).toFloat()
        oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
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

        // Draw the ring.
        paint.color = ColorUtils.blendARGB(ringColor, Color.BLACK, 0.25f)
        canvas.drawArc(oval, 0f, 360f, false, paint)
        paint.color = ringColor
        canvas.drawArc(oval, -90f, progress / 100f * 360f, false, paint)
    }
}