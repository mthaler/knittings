package com.mthaler.knittings

import android.graphics.Color
import android.support.v4.app.ListFragment
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.details.EditKnittingDetailsActivity
import com.mthaler.knittings.details.KnittingDetailsActivity
import com.mthaler.knittings.model.Knitting
import org.jetbrains.anko.AnkoLogger
import java.text.DateFormat
import java.util.Date
import org.jetbrains.anko.support.v4.*

/**
 * KnittingListFragment displays a list of knittings
 */
class KnittingListFragment : ListFragment(), KnittingListView, AnkoLogger {

    private var _sorting: Sorting = Sorting.NewestFirst
    private var _filter: Filter = NoFilter

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        // get current knitting and start knitting details activitiy if it is not null
        (listAdapter as KnittingAdapter).getItem(position)?.let {
            startActivity<KnittingDetailsActivity>(KnittingDetailsActivity.EXTRA_KNITTING_ID to it.id)
        }
    }

    override fun onResume() {
        super.onResume()
        updateKnittingList()
    }

    override fun addKnitting() {
        // start knitting activity with newly created knitting
        val knitting = datasource.createKnitting("", "", Date(), null, 0.0, 0.0, 0.0)
        startActivity<EditKnittingDetailsActivity>(EditKnittingDetailsActivity.EXTRA_KNITTING_ID to knitting.id)
    }

    override fun updateKnittingList() {
        val knittings = datasource.allKnittings
        when(_sorting) {
            Sorting.NewestFirst -> knittings.sortByDescending { it.started}
            Sorting.OldestFirst -> knittings.sortBy { it.started }
            Sorting.Alphabetical -> knittings.sortBy { it.title.toLowerCase() }
        }
        val filtered = _filter.filter(knittings)
        val adapter = KnittingAdapter(filtered)
        listAdapter = adapter
    }

    override fun getSorting(): Sorting = _sorting

    override fun setSorting(sorting: Sorting) {
        _sorting = sorting
    }

    override fun getFilter(): Filter = _filter

    override fun setFilter(filter: Filter) {
        _filter = filter
    }


    private inner class KnittingAdapter(knittings: List<Knitting>) : ArrayAdapter<Knitting>(activity, android.R.layout.simple_list_item_1, knittings) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

            val v = if (convertView != null) convertView else activity!!.layoutInflater.inflate(R.layout.list_item_knitting, parent, false)

            // configure the view for the knitting
            val knitting = getItem(position)

            val titleTextView = v.findViewById<TextView>(R.id.knitting_list_item_titleTextView)
            titleTextView.text = knitting.title

            val descriptionTextView = v.findViewById<TextView>(R.id.knitting_list_item_descriptionTextView)
            descriptionTextView.text = knitting.description

            val startedTextView = v.findViewById<TextView>(R.id.knitting_list_item_startedTextView)
            startedTextView.text = DateFormat.getDateInstance().format(knitting.started)

            val photoView = v.findViewById<ImageView>(R.id.knitting_list_item_photoImageView)
            // the list item views are reused, we always need to set bitmap, otherwise the previous bitmap is used
            if (knitting.defaultPhoto?.preview != null) {
                photoView.setImageBitmap(knitting.defaultPhoto.preview)
            } else {
                photoView.setImageBitmap(null)
            }

            val categoryIndicator = v.findViewById<CategoryIndicator>(R.id.knitting_list_item_categoryIndicator)
            if (knitting.category != null) {
                if (knitting.category.color != null) {
                    categoryIndicator.color = knitting.category.color
                } else {
                    categoryIndicator.color = Color.WHITE
                }
            } else {
                categoryIndicator.color = Color.WHITE
            }

            return v
        }
    }
}
