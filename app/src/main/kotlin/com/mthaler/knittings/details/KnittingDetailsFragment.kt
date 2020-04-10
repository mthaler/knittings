package com.mthaler.knittings.details

import android.content.Context
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.Extras
import com.mthaler.knittings.photo.ImageAdapter
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.utils.TimeUtils
import org.jetbrains.anko.AnkoLogger
import java.text.DateFormat

/**
 * Fragment that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsFragment : Fragment(), AnkoLogger {

    private var knittingID: Long = -1
    private lateinit var viewModel: KnittingDetailsViewModel
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(Extras.EXTRA_KNITTING_ID)
        }

        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_knitting_details, container, false)

        // start edit knitting details fragment if the user clicks the floating action button
        val fabEdit = v.findViewById<FloatingActionButton>(R.id.edit_knitting_details)
        fabEdit.setOnClickListener {
            listener?.editKnitting(knittingID)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(KnittingDetailsViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.knitting.observe(viewLifecycleOwner, Observer { knitting ->
            updateDetails(knitting)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.knitting_details_fragment, menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_show_stopwatch -> {
                listener?.startStopwatch(knittingID)
                true
            }
            R.id.menu_item_delete_knitting -> {
                listener?.deleteKnitting(knittingID)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateDetails(knittingWithPhotos: KnittingWithPhotos) {
        view?.let {
            // remove slider dots and on page changed listener. We readd it if we need it
            val sliderDots = it.findViewById<LinearLayout>(R.id.sliderDots)
            sliderDots.removeAllViews()
            val viewPager = it.findViewById<ViewPager>(R.id.view_pager)
            viewPager.clearOnPageChangeListeners()
            viewPager.offscreenPageLimit = 3
            val photos = knittingWithPhotos.photos
            photos.sortByDescending { it.id }
            if (photos.size > 0) {
                viewPager.visibility = View.VISIBLE
                val adapter = ImageAdapter(it.context, photos) // Here we are defining the Imageadapter object
                viewPager.adapter = adapter // Here we are passing and setting the adapter for the images

                val dotscount = adapter.count

                if (photos.size > 1) {
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
                    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

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
                viewPager.visibility = View.GONE
            }

            val knitting = knittingWithPhotos.knitting

            val knittingTitle = it.findViewById<TextView>(R.id.knitting_title)
            knittingTitle.text = knitting.title

            val knittingDescription = it.findViewById<TextView>(R.id.knitting_description)
            knittingDescription.text = knitting.description

            val knittingStarted = it.findViewById<TextView>(R.id.knitting_started)
            knittingStarted.text = getString(R.string.knitting_details_started, DateFormat.getDateInstance().format(knitting.started))

            val knittingFinished = it.findViewById<TextView>(R.id.knitting_finished)
            knittingFinished.text = getString(R.string.knitting_details_finished, if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else "")

            val knittingNeedleDiameter = it.findViewById<TextView>(R.id.knitting_needle_diameter)
            knittingNeedleDiameter.text = getString(R.string.knitting_details_needle, knitting.needleDiameter)

            val knittingSize = it.findViewById<TextView>(R.id.knitting_size)
            knittingSize.text = getString(R.string.knitting_details_size, knitting.size)

            val knittingDuration = it.findViewById<TextView>(R.id.knitting_duration)
            knittingDuration.text = getString(R.string.knitting_details_duration, TimeUtils.formatDuration(knitting.duration))

            val knittingCategory = it.findViewById<TextView>(R.id.knitting_category)
            val c = knitting.category
            knittingCategory.text = getString(R.string.knitting_details_category, c?.name ?: "")

            val knittingStatus = it.findViewById<TextView>(R.id.knitting_status)
            knittingStatus.text = getString(R.string.knitting_details_status, Status.format(it.context, knitting.status))

            val ratingBar = it.findViewById<RatingBar>(R.id.ratingBar)
            ratingBar.rating = knitting.rating.toFloat()
        }
    }

    /**
     * Called when a fragment is first attached to its context. onCreate(Bundle) will be called after this.
     *
     * @param context Context
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity. This is called after onDestroy().
     */
    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {

        /**
         * Called if the user clicks the floating action button to edit a knitting project
         */
        fun editKnitting(id: Long)

        fun startStopwatch(id: Long)

        fun deleteKnitting(id: Long)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param knittingID id of the knitting project that should be displayed
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
