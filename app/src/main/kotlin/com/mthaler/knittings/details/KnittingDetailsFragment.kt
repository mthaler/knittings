package com.mthaler.knittings.details

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.mthaler.knittings.photo.ImageAdapter
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.utils.TimeUtils
import org.jetbrains.anko.AnkoLogger
import java.text.DateFormat
import kotlinx.android.synthetic.main.fragment_knitting_details.*

/**
 * Fragment that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsFragment : Fragment(), AnkoLogger {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_knitting_details, container, false)

    /**
     * Initializes the fragment with the given knitting
     *
     * @param knitting knitting for which details should be displayed
     */
    fun init(knitting: Knitting) {
        view?.let {
            // remove slider dots and on page changed listener. We readd it if we need it
            sliderDots.removeAllViews()
            view_pager.clearOnPageChangeListeners()
            view_pager.offscreenPageLimit = 3
            val photos = datasource.getAllPhotos(knitting)
            if (photos.size > 0) {
                view_pager.visibility = View.VISIBLE
                val adapter = ImageAdapter(context!!, photos) //Here we are defining the Imageadapter object
                view_pager.adapter = adapter // Here we are passing and setting the adapter for the images


                val dotscount = adapter.count

                if (photos.size > 1)  {
                    // create array of dots that are displayed at the bottom of the photo
                    val dots = (0 .. dotscount - 1).map {
                        val dot = ImageView(context)
                        dot.setImageDrawable(ContextCompat.getDrawable(context!!.applicationContext, R.drawable.non_active_dot))
                        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        params.setMargins(8, 0, 8, 0)
                        sliderDots.addView(dot, params)
                        dot
                    }.toTypedArray()
                    // make the first dot active
                    dots[0].setImageDrawable(ContextCompat.getDrawable(context!!.applicationContext, R.drawable.active_dot))
                    view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                        override fun onPageSelected(position: Int) {
                            context?.let {
                                for (i in 0 until dotscount) {
                                    dots[i].setImageDrawable(ContextCompat.getDrawable(it.applicationContext, R.drawable.non_active_dot))
                                }
                                // mark the dot for the selected page as active
                                dots[position].setImageDrawable(ContextCompat.getDrawable(it.applicationContext, R.drawable.active_dot))
                            }

                        }

                        override fun onPageScrollStateChanged(state: Int) {}
                    })
                }
            } else {
                view_pager.visibility = View.GONE
            }


            knitting_title.text = knitting.title
            knitting_description.text = knitting.description
            knitting_started.text = getString(R.string.knitting_details_started, DateFormat.getDateInstance().format(knitting.started))
            knitting_finished.text = getString(R.string.knitting_details_finished, if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else "")
            knitting_needle_diameter.text = getString(R.string.knitting_details_needle, knitting.needleDiameter)
            knitting_size.text = getString(R.string.knitting_details_size, knitting.size)
            knitting_duration.text = getString(R.string.knitting_details_duration, TimeUtils.formatDuration(knitting.duration))
            knitting_category.text = getString(R.string.knitting_details_category, if (knitting.category != null) knitting.category.name else "")
            knitting_status.text = getString(R.string.knitting_details_status, knitting.status)
            ratingBar.rating = knitting.rating.toFloat()
        }
    }
}
