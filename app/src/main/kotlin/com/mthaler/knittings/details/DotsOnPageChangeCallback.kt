package com.mthaler.knittings.details

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.mthaler.knittings.R

class DotsOnPageChangeCallback(private val context: Context, private val dotscount: Int, sliderDots: LinearLayout) : ViewPager2.OnPageChangeCallback() {

    val dots = (0..dotscount - 1).map {
        val dot = ImageView(context)
        dot.setImageDrawable(ContextCompat.getDrawable(context.applicationContext, R.drawable.non_active_dot))
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(8, 0, 8, 0)
        sliderDots.addView(dot, params)
        dot
    }.toTypedArray()

    init {
        // make the first dot active
        dots[0].setImageDrawable(ContextCompat.getDrawable(context.applicationContext, R.drawable.active_dot))
    }

    override fun onPageSelected(position: Int) {
        for (i in 0 until dotscount) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(context.applicationContext, R.drawable.non_active_dot))
        }
        // mark the dot for the selected page as active
        dots[position].setImageDrawable(ContextCompat.getDrawable(context.applicationContext, R.drawable.active_dot))
    }
}