package com.mthaler.knittings.photo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
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
import java.io.File

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment(), AnkoLogger {

    private lateinit var knitting: Knitting
    private lateinit var gridView: GridView
    private var currentPhotoPath: File? = null

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
            val knittingID = it.getLong(EXTRA_KNITTING_ID)
            knitting = datasource.getKnitting(knittingID)
        }
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
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_KNITTING_ID)) {
                knitting = datasource.getKnitting(savedInstanceState.getLong(EXTRA_KNITTING_ID))
                debug("Set knitting: $knitting")
            }
        }

        gridView = v.findViewById(R.id.gridView)

        val photos = datasource.getAllPhotos(knitting)
        val gridAdapter = GridViewAdapter(activity!!, R.layout.grid_item_layout, photos)
        gridView.adapter = gridAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // photo clicked, show photo in photo activity
            val photo = parent.getItemAtPosition(position) as Photo
            startActivity<PhotoActivity>(EXTRA_PHOTO_ID to photo.id, EXTRA_KNITTING_ID to knitting.id)
        }

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
        outState.putLong(EXTRA_KNITTING_ID, k.id)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val a = activity
        if (a != null) {
            // get knitting from database because it could have changed
            knitting = datasource.getKnitting(knitting.id)
            val photos = datasource.getAllPhotos(knitting)
            val gridAdapter = GridViewAdapter(a, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        }
    }

    /**
     * Initialize the contents of the Fragment host's standard options menu. You should place your menu items in to menu.
     * For this method to be called, you must have first called setHasOptionsMenu(boolean).
     * See Activity.onCreateOptionsMenu for more information.
     *
     * @param menu The options menu in which you place your items.
     * @param inflater MenuInflater
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        if (menu != null && inflater != null) {
            inflater.inflate(R.menu.photo_gallery, menu)
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            return when (item.itemId) {
                R.id.menu_item_add_photo -> {
                    val d = TakePhotoDialog.create(context!!, layoutInflater, knitting.id, this::takePhoto, this::importPhoto)
                    d.show()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with, the resultCode it returned,
     * and any additional data from it. The resultCode will be RESULT_CANCELED if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                currentPhotoPath?.let { TakePhotoDialog.handleTakePhotoResult(context!!, knitting.id, it) }
            }
            REQUEST_IMAGE_IMPORT -> {
                val f = currentPhotoPath
                if (f != null && data != null) {
                    TakePhotoDialog.handleImageImportResult(context!!, knitting.id, f, data)
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun takePhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    private fun importPhoto(file: File, intent: Intent) {
        currentPhotoPath = file
        startActivityForResult(intent, REQUEST_IMAGE_IMPORT)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         *
         * @param knittingID id of the knitting for which photos should be displayed
         * @return A new instance of fragment PhotoGalleryFragment.
         */
        @JvmStatic
        fun newInstance(knittingID: Long) =
            PhotoGalleryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }

        const val REQUEST_IMAGE_CAPTURE = 0
        const val REQUEST_IMAGE_IMPORT = 1
    }
}
