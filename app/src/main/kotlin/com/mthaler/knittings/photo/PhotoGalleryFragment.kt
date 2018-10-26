package com.mthaler.knittings.photo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import com.mthaler.knittings.R
import org.jetbrains.anko.support.v4.*
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Photo
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment(), AnkoLogger {

    private var knitting: Knitting? = null

    private lateinit var gridView: GridView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = datasource.getKnitting(savedInstanceState.getLong(KNITTING_ID))
                debug("Set knitting: $knitting")
            }
        }

        gridView = v.findViewById(R.id.gridView)

        return v
    }

    override fun onResume() {
        super.onResume()
        val k = knitting
        val a = activity
        if (k != null && a != null) {
            // get knitting from database because it could have changed
            knitting = datasource.getKnitting(k.id)
            val photos = datasource.getAllPhotos(k)
            val gridAdapter = GridViewAdapter(a, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val k = knitting
        if (k != null) {
            outState.putLong(KNITTING_ID, k.id)
        }
        super.onSaveInstanceState(outState)
    }

    fun init(knitting: Knitting) {
        this.knitting = knitting
        val photos = datasource.getAllPhotos(knitting)
        val gridAdapter = GridViewAdapter(activity!!, R.layout.grid_item_layout, photos)
        gridView.adapter = gridAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // photo clicked, show photo in photo activity
            val photo = parent.getItemAtPosition(position) as Photo
            startActivity<PhotoActivity>(PhotoActivity.EXTRA_PHOTO_ID to photo.id, PhotoActivity.EXTRA_KNITTING_ID to knitting.id)
        }
    }

    companion object : AnkoLogger {

        private const val KNITTING_ID = "knitting_id"
    }
}
