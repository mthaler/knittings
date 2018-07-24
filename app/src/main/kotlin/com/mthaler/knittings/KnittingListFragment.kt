package com.mthaler.knittings

import android.support.v4.app.ListFragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import org.jetbrains.anko.AnkoLogger
import java.text.DateFormat
import java.util.ArrayList
import java.util.Date
import org.jetbrains.anko.support.v4.*

class KnittingListFragment : ListFragment(), KnittingListView, AnkoLogger {

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        // get current knitting
        val c = (listAdapter as KnittingAdapter).getItem(position)
        // start knitting activity
        startActivity<KnittingDetailsActivity>(KnittingDetailsActivity.EXTRA_KNITTING_ID to c!!.id)
    }

    override fun onResume() {
        super.onResume()
        val knittings = datasource.allKnittings
        knittings.sortByDescending { x -> x.started }
        val adapter = KnittingAdapter(knittings)
        listAdapter = adapter
    }

    override fun addKnitting() {
        // start knitting activity with newly created knitting
        val knitting = datasource.createKnitting("", "", Date(), null, 0.0, 0.0, 0.0)
        startActivity< EditKnittingDetailsActivity>(EditKnittingDetailsActivity.EXTRA_KNITTING_ID to knitting.id)
    }

    private inner class KnittingAdapter(knittings: ArrayList<Knitting>) : ArrayAdapter<Knitting>(activity, android.R.layout.simple_list_item_1, knittings) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = activity!!.layoutInflater
                        .inflate(R.layout.list_item_knitting, parent, false)
            }

            // configure the view for this Crime
            val knitting = getItem(position)

            val titleTextView = convertView!!.findViewById<TextView>(R.id.knitting_list_item_titleTextView)
            titleTextView.text = knitting.title

            val descriptionTextView = convertView.findViewById<TextView>(R.id.knitting_list_item_descriptionTextView)
            descriptionTextView.text = knitting.description

            val startedTextView = convertView.findViewById<TextView>(R.id.knitting_list_item_startedTextView)
            startedTextView.text = DateFormat.getDateInstance().format(knitting.started)

            val photoView = convertView.findViewById<ImageView>(R.id.knitting_list_item_photoImageView)
            // the list item views are reused, we always need to set bitmap, otherwise the previous bitmap is used
            if (knitting.defaultPhoto?.preview != null) {
                photoView.setImageBitmap(knitting.defaultPhoto.preview)
            } else {
                photoView.setImageBitmap(null)
            }

            return convertView
        }
    }
}
