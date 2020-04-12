package com.mthaler.knittings.photo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import java.io.File

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment() {

    private var knittingID: Long = -1
    private var currentPhotoPath: File? = null
    private lateinit var gridView: GridView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            knittingID = it.getLong(EXTRA_KNITTING_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_KNITTING_ID)) {
                knittingID = savedInstanceState.getLong(EXTRA_KNITTING_ID)
            }
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH))
            }
        }

        gridView = v.findViewById(R.id.gridView)
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            // photo clicked, show photo in photo activity
            val photo = parent.getItemAtPosition(position) as Photo
            startActivity(PhotoActivity.newIntent(context!!, photo.id))
        }

        return v
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_KNITTING_ID, knittingID)
        currentPhotoPath?.let { savedInstanceState.putString(CURRENT_PHOTO_PATH, it.absolutePath) }
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(activity!!.application)).get(PhotoGalleryViewModel::class.java)
        viewModel.init(knittingID)
        viewModel.photos.observe(viewLifecycleOwner, Observer { photos ->
            // show the newest photos first. The id is incremented for each photo that is added, thus we can sort by id
            photos.sortByDescending { it.id }
            val gridAdapter = GridViewAdapter(activity!!, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo_gallery, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_add_photo -> {
                context?.let {
                    val d = TakePhotoDialog.create(it, layoutInflater, knittingID, this::takePhoto, this::importPhoto)
                    d.show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                currentPhotoPath?.let { TakePhotoDialog.handleTakePhotoResult(context!!, knittingID, it) }
            }
            REQUEST_IMAGE_IMPORT -> {
                val f = currentPhotoPath
                if (f != null && data != null) {
                    TakePhotoDialog.handleImageImportResult(context!!, knittingID, f, data)
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

        @JvmStatic
        fun newInstance(knittingID: Long) =
            PhotoGalleryFragment().apply {
                arguments = Bundle().apply {
                    putLong(EXTRA_KNITTING_ID, knittingID)
                }
            }

        const val CURRENT_PHOTO_PATH = "com.mthaler.knitting.CURRENT_PHOTO_PATH"
        const val REQUEST_IMAGE_CAPTURE = 0
        const val REQUEST_IMAGE_IMPORT = 1
    }
}
