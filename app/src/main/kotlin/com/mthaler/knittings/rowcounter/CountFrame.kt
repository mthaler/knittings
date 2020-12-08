package com.mthaler.knittings.rowcounter

import android.content.Context
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout

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
        strokeWidth = 25f
        strokeCap = Paint.Cap.BUTT
    }

    private var rect = RectF(0f, 0f, 1f, 1f)

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
        val cx = (width / 2.0).toFloat()
        val cy = (height / 2.0).toFloat()
        rect = RectF((cx - width / 2.0 * 0.95).toFloat(), (cy - height / 2.0 * 0.95).toFloat(), (cx + width / 2.0 * 0.95).toFloat(), (cy + height / 2.0 * 0.95).toFloat())
    }
}