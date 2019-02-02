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
import com.mthaler.knittings.Extras
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

    private lateinit var knitting: Knitting

    /**
     * Called to do initial creation of a fragment. This is called after onAttach(Activity) and before
     * onCreateView(LayoutInflater, ViewGroup, Bundle). Note that this can be called while the fragment's activity
     * is still in the process of being created. As such, you can not rely on things like the activity's content view
     * hierarchy being initialized at this point. If you want to do work once the activity itself is created,
     * see onActivityCreated(Bundle).
     *
     * Any restored child fragments will be created before the base Fragment.onCreate method returns.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
            knitting = datasource.getKnitting(knittingID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    /**
     * Called to have the fragment instantiate its user interface view. This is optional, and non-graphical
     * fragments can return null (which is the default implementation). This will be called between onCreate(Bundle)
     * and onActivityCreated(Bundle).
     *
     * If you return a View from here, you will later be called in onDestroyView() when the view is being released.
     *
     * @param inflater the LayoutInflater object that can be used to inflate any views in the fragment
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *                  The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_knitting_details, container, false)

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
        val c = knitting.category
        knitting_category.text = getString(R.string.knitting_details_category, if (c != null) c.name else "")
        knitting_status.text = getString(R.string.knitting_details_status, knitting.status)
        ratingBar.rating = knitting.rating.toFloat()

        return v
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param categoryID id of the category that should be edited
         * @return A new instance of fragment KnittingDetailsFragment
         */
        @JvmStatic
        fun newInstance(knittingID: Long) =
                KnittingDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putLong(Extras.EXTRA_KNITTING_ID, knittingID)
                    }
                }

    }
}
