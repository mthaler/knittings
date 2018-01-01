package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import org.jetbrains.anko.AnkoLogger
import java.text.DateFormat

/**
 * Fragment that displays knitting details (name, description, start time etc.)
 */
class KnittingDetailsFragment : Fragment(), AnkoLogger {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_knitting_details, container, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                val knitting = datasource.getKnitting(savedInstanceState.getLong(KNITTING_ID))
                init(knitting)
            }
        }

        return v
    }

    /**
     * Initializes the fragment with the given knitting
     *
     * @param knitting knitting for which details should be displayed
     */
    fun init(knitting: Knitting) {
        val v = view
        if (v != null) {
            val viewPager = v.findViewById<ViewPager>(R.id.view_pager)
            val adapter = ImageAdapter(context!!, datasource.getAllPhotos(knitting)) //Here we are defining the Imageadapter object
            viewPager.adapter = adapter // Here we are passing and setting the adapter for the images

            val textViewTitle = v.findViewById<TextView>(R.id. knitting_title2)
            textViewTitle.text = knitting.title

            val textViewDescription = v.findViewById<TextView>(R.id. knitting_description2)
            textViewDescription.text = knitting.description

            val textViewStarted = v.findViewById<TextView>(R.id.knitting_started2)
            textViewStarted.text = "Started: ${DateFormat.getDateInstance().format(knitting.started)}"

            val textViewFinished = v.findViewById<TextView>(R.id.knitting_finished2)
            textViewFinished.text = "Finished: ${if (knitting.finished != null) DateFormat.getDateInstance().format(knitting.finished) else ""}"

            val textViewNeedleDiameter = v.findViewById<TextView>(R.id.knitting_needle_diameter2)
            textViewNeedleDiameter.text = "Needle: ${java.lang.Double.toString(knitting.needleDiameter)}"

            val textViewSize = v.findViewById<TextView>(R.id.knitting_size2)
            textViewSize.text = "Size: ${java.lang.Double.toString(knitting.size)}"

            val ratingBar = v.findViewById<RatingBar>(R.id.ratingBar2)
            ratingBar.rating = knitting.rating.toFloat()
        }
    }

    companion object: AnkoLogger {
        val KNITTING_ID = "knitting_id"
    }
}
