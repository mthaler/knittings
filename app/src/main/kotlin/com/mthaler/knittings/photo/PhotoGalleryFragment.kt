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
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.Extras.EXTRA_PHOTO_ID

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment(), AnkoLogger {

    private var knitting: Knitting? = null
    private lateinit var gridView: GridView

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
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_KNITTING_ID)) {
                knitting = datasource.getKnitting(savedInstanceState.getLong(EXTRA_KNITTING_ID))
                debug("Set knitting: $knitting")
            }
        }

        gridView = v.findViewById(R.id.gridView)

        return v
    }

    /**
     * This method is called if the activity gets destroyed because e.g. the device configuration changes because the device is rotated
     * We need to store instance variables because they are not automatically restored
     *
     * @param savedInstanceState saved instance state
     */
    override fun onSaveInstanceState(outState: Bundle) {
        val k = knitting
        if (k != null) {
            outState.putLong(EXTRA_KNITTING_ID, k.id)
        }
        super.onSaveInstanceState(outState)
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

    fun init(knitting: Knitting) {
        this.knitting = knitting
        val photos = datasource.getAllPhotos(knitting)
        val gridAdapter = GridViewAdapter(activity!!, R.layout.grid_item_layout, photos)
        gridView.adapter = gridAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // photo clicked, show photo in photo activity
            val photo = parent.getItemAtPosition(position) as Photo
            startActivity<PhotoActivity>(EXTRA_PHOTO_ID to photo.id, EXTRA_KNITTING_ID to knitting.id)
        }
    }
}
