package com.mthaler.knittings

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import java.io.File

/**
 * A fragment that displays a list of photos using a grid
 */
class PhotoGalleryFragment : Fragment() {

    private var knitting: Knitting? = null

    private lateinit var gridView: GridView
    private var currentPhotoPath: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(CURRENT_PHOTO_PATH)) {
                currentPhotoPath = File(savedInstanceState.getString(CURRENT_PHOTO_PATH)!!)
                Log.d(LOG_TAG, "Set current photo path: " + currentPhotoPath!!)
            }
            if (savedInstanceState.containsKey(KNITTING_ID)) {
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(savedInstanceState.getLong(KNITTING_ID))
                Log.d(LOG_TAG, "Set knitting: " + knitting!!)
            }
        } else {
            val knittingID = arguments.getLong(KNITTING_ID)
            knitting = KnittingsDataSource.getInstance(activity).getKnitting(knittingID)
            Log.d(LOG_TAG, "Set knitting: " + knitting!!)
        }

        gridView = v.findViewById(R.id.gridView)
        val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
        val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
        gridView.adapter = gridAdapter
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, v, position, id ->
            if (position < gridView.adapter.count - 1) {
                val photo = parent.getItemAtPosition(position) as Photo
                //Create intent
                val intent = Intent(activity, PhotoActivity::class.java)
                intent.putExtra(PhotoActivity.EXTRA_PHOTO_ID, photo.id)
                intent.putExtra(KnittingActivity.EXTRA_KNITTING_ID, knitting!!.id)
                Log.d(LOG_TAG, "Created PhotoActivity intent")

                //Start details activity
                startActivity(intent)
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val photoFile = KnittingsDataSource.getInstance(activity).getPhotoFile(knitting!!)
                currentPhotoPath = photoFile
                Log.d(LOG_TAG, "Set current photo path: " + currentPhotoPath!!)
                val packageManager = activity.packageManager
                val canTakePhoto = photoFile != null && takePictureIntent.resolveActivity(packageManager) != null
                if (canTakePhoto) {
                    val uri = FileProvider.getUriForFile(context, "com.mthaler.knittings.fileprovider", photoFile)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                    Log.d(LOG_TAG, "Created take picture intent")
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // add photo to database
            Log.d(LOG_TAG, "Received result for take photo intent")
            val orientation = PictureUtils.getOrientation(currentPhotoPath!!.absolutePath)
            val preview = PictureUtils.decodeSampledBitmapFromPath(currentPhotoPath!!.absolutePath, 200, 200)
            val rotatedPreview = PictureUtils.rotateBitmap(preview, orientation)
            val photo = KnittingsDataSource.getInstance(activity).createPhoto(currentPhotoPath!!, knitting!!.id, rotatedPreview, "")
            Log.d(LOG_TAG, "Created new photo from " + currentPhotoPath + ", knitting id " + knitting!!.id)
            // add first photo as default photo
            if (knitting!!.defaultPhoto == null) {
                Log.d(LOG_TAG, "Set $photo as default photo")
                knitting!!.defaultPhoto = photo
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
            // update grid view
            val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
            val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
            gridView!!.adapter = gridAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        if (knitting != null) {
            val photos = KnittingsDataSource.getInstance(activity).getAllPhotos(knitting!!)
            val gridAdapter = GridViewAdapter(activity, R.layout.grid_item_layout, photos)
            gridView.adapter = gridAdapter
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (currentPhotoPath != null) {
            outState!!.putString(CURRENT_PHOTO_PATH, currentPhotoPath!!.absolutePath)
        }
        if (knitting != null) {
            outState!!.putLong(KNITTING_ID, knitting!!.id)
        }
        super.onSaveInstanceState(outState)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (knitting != null) {
            if (isVisibleToUser) {
                // the fragment became visible because the user selected it in the view pager
                // get current knitting from database
                knitting = KnittingsDataSource.getInstance(activity).getKnitting(knitting!!.id)
            } else {
                // the fragment became invisible because the user selected another tab in the view pager
                // save current knitting to database
                KnittingsDataSource.getInstance(activity).updateKnitting(knitting!!)
            }
        }
    }

    companion object {

        private val CURRENT_PHOTO_PATH = "current_photo_path"
        private val KNITTING_ID = "knitting_id"
        private val REQUEST_IMAGE_CAPTURE = 0
        private val LOG_TAG = PhotoGalleryFragment::class.java.simpleName

        fun newInstance(knitting: Knitting): PhotoGalleryFragment {
            val fragment = PhotoGalleryFragment()
            val args = Bundle()
            args.putLong(KNITTING_ID, knitting.id)
            fragment.arguments = args
            Log.d(LOG_TAG, "Created new PhotoGalleryFragment with knitting id: " + knitting.id)
            return fragment
        }
    }
}
