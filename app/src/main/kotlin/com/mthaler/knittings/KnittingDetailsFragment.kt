package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
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
        val v = view
        if (v != null) {
            view_pager.offscreenPageLimit = 3
            val photos = datasource.getAllPhotos(knitting)
            if (photos.size > 0) {
                view_pager.visibility = View.VISIBLE
                val adapter = ImageAdapter(context!!, photos) //Here we are defining the Imageadapter object
                view_pager.adapter = adapter // Here we are passing and setting the adapter for the images
            } else {
                view_pager.visibility = View.GONE
            }

            knitting_title.text = knitting.title
            knitting_description.text = knitting.description
            knitting_started.text = getString(R.string.knitting_details_started, DateFormat.getDateInstance().format(knitting.started))
            knitting_finished.text = getString(R.string.knitting_details_finished, if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else "")
            knitting_needle_diameter.text = getString(R.string.knitting_details_needle, knitting.needleDiameter)
            knitting_size.text = getString(R.string.knitting_details_size, knitting.size)
            ratingBar.rating = knitting.rating.toFloat()
        }
    }
}
