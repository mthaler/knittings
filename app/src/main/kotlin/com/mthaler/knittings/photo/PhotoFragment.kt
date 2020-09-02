package com.mthaler.knittings.photo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.core.content.FileProvider
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import com.mthaler.knittings.R
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.Extras.EXTRA_PHOTO_ID
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.preference.PreferenceManager
import androidx.lifecycle.lifecycleScope
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.DeleteDialog
import com.mthaler.knittings.SaveChangesDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PhotoFragment displays a photo and the description
 */
class PhotoFragment : Fragment() {

    private lateinit var photo: Photo
    private lateinit var editTextDescription: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val photoID = it.getLong(EXTRA_PHOTO_ID)
            photo = datasource.getPhoto(photoID)
        }
        savedInstanceState?.let {
            if (it.containsKey(EXTRA_PHOTO_ID)) {
                val photoID = it.getLong(EXTRA_PHOTO_ID)
                photo = datasource.getPhoto(photoID)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {

        // we need to call setHasOptionsMenu(true) to indicate that we are interested in participating in the action bar
        setHasOptionsMenu(true)

        val v = inflater.inflate(R.layout.fragment_photo, parent, false)

        val imageView = v.findViewById<ImageView>(R.id.image)
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        val viewTreeObserver = imageView.viewTreeObserver
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                imageView.viewTreeObserver.removeOnPreDrawListener(this)
                val width = imageView.measuredWidth
                val height = imageView.measuredHeight
                // loading and scaling the bitmap is expensive, use async task to do the work
                val path = photo.filename.absolutePath
                viewLifecycleOwner.lifecycleScope.launch {
                    val rotated = withContext(Dispatchers.Default) {
                        val orientation = PictureUtils.getOrientation(path)
                        val scaled = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                        PictureUtils.rotateBitmap(scaled, orientation)
                    }
                    imageView.setImageBitmap(rotated)
                }
                return true
            }
        })

        editTextDescription = v.findViewById(R.id.photo_description)

        if (savedInstanceState == null) {
            editTextDescription.setText(photo.description)
        }

        return v
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(EXTRA_PHOTO_ID, photo.id)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.photo, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_photo -> {
                DeleteDialog.create(requireContext(), editTextDescription.text.toString()) {
                    deletePhoto()
                    parentFragmentManager.popBackStack()
                }.show()
                true
            }
            R.id.menu_item_set_main_photo -> {
                setDefaultPhoto()
                true
            }
            R.id.menu_item_rotate_photo -> {
                true
            }
            R.id.menu_item_share -> {
                val path = photo.filename.absolutePath
                val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                val size = Integer.parseInt(prefs.getString(resources.getString(R.string.key_share_photo_size), "1200"))
                val scaled = PictureUtils.decodeSampledBitmapFromPath(path, size, size)
                val uri = saveImage(scaled)
                uri?.let { shareImageUri(it) }
                true
            }
            R.id.menu_item_save_photo -> {
                if (editTextDescription.text.toString() != photo.description) {
                    savePhoto(photo.copy(description = editTextDescription.text.toString()))
                }
                parentFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onBackPressed() {
        if (editTextDescription.text.toString() != photo.description) {
            SaveChangesDialog.create(requireContext(), {
                savePhoto(photo.copy(description = editTextDescription.text.toString()))
                parentFragmentManager.popBackStack()
            }, {
                parentFragmentManager.popBackStack()
            }).show()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setDefaultPhoto() {
        val knitting = datasource.getKnitting(photo.ownerID)
        datasource.updateKnitting(knitting.copy(defaultPhoto = photo))
        view?.let {
            Snackbar.make(it, "Used as main photo", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun saveImage(image: Bitmap): Uri? {
        // TODO - Should be processed in another thread
        var uri: Uri? = null
        val imagesFolder = File(requireContext().cacheDir, "images")
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.jpg")

            val stream = FileOutputStream(file)
            image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            stream.flush()
            stream.close()
            uri = FileProvider.getUriForFile(requireContext(), "com.mthaler.knittings.fileprovider", file)
        } catch (e: IOException) {
            error("IOException while trying to write file for sharing: " + e.message)
        }
        return uri
    }

    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }

    private fun deletePhoto() {
        datasource.deletePhoto(photo)
    }

    private fun savePhoto(photo: Photo) {
        if (photo.id == -1L) {
            datasource.addPhoto(photo)
        } else {
            datasource.updatePhoto(photo)
        }
    }

    companion object {

        fun newInstance(photoID: Long): PhotoFragment {
            val bundle = Bundle()
            bundle.putLong(EXTRA_PHOTO_ID, photoID)

            val fragment = PhotoFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
