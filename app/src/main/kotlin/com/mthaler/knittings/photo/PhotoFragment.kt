package com.mthaler.knittings.photo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.text.Editable
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import com.mthaler.knittings.R
import com.mthaler.knittings.TextWatcher
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.alert
import com.mthaler.knittings.Extras.EXTRA_PHOTO_ID
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.preference.PreferenceManager

/**
 * PhotoFragment displays a photo and the description
 */
class PhotoFragment : Fragment(), AnkoLogger {

    var photo: Photo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // we need to call setHasOptionsMenu(true) to indicate that we are interested in participating in the action bar
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_photo, parent, false)

        arguments?.let {
            val id = it.getLong(EXTRA_PHOTO_ID, -1L)
            if (id != -1L) {
                photo = datasource.getPhoto(id)
            }
        }

        val imageView = v.findViewById<ImageView>(R.id.image)
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        val viewTreeObserver = imageView.viewTreeObserver
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                imageView.viewTreeObserver.removeOnPreDrawListener(this)
                val width = imageView.measuredWidth
                val height = imageView.measuredHeight
                // loading and scaling the bitmap is expensive, use async task to do the work
                photo?.let {
                    val path = it.filename.absolutePath
                    doAsync {
                        val orientation = PictureUtils.getOrientation(path)
                        val scaled = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                        val rotated = PictureUtils.rotateBitmap(scaled, orientation)
                        uiThread {
                            imageView.setImageBitmap(rotated)
                        }
                    }
                }
                return true
            }
        })

        val editTextDescription = v.findViewById<EditText>(R.id.photo_description)
        editTextDescription.setText(photo?.description ?: "")
        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(c: Editable) {
                val p0 = photo
                if (p0 != null) {
                    val p1 = p0.copy(description = c.toString())
                    datasource.updatePhoto(p1)
                    photo = p1
                }
            }
        })

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater?.inflate(R.menu.photo, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.menu_item_delete_photo -> {
                showDeletePhotoDialog()
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
                photo?.let {
                    val path = it.filename.absolutePath
                    val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
                    val size = Integer.parseInt(prefs.getString(resources.getString(R.string.key_share_photo_size), "1200"))
                    val scaled = PictureUtils.decodeSampledBitmapFromPath(path, size, size)
                    val uri = saveImage(scaled)
                    uri?.let { shareImageUri(it) }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * how alert asking user to confirm that photo should be deleted
     */
    private fun showDeletePhotoDialog() {
        alert {
            title = resources.getString(R.string.delete_photo_dialog_title)
            message = resources.getString(R.string.delete_photo_dialog_question)
            positiveButton(resources.getString(R.string.delete_photo_dialog_delete_button)) {
                deletePhoto()
                // close activity
                activity?.finish()
            }
            negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
        }.show()
    }


    private fun deletePhoto() {
        // check if the photo is used as default photo
        photo?.let {
            val knitting = datasource.getKnitting(it.knittingID)
            if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == it.id) {
                datasource.updateKnitting(knitting.copy(defaultPhoto = null))
            }
            // delete database entry
            datasource.deletePhoto(it)
            photo = null
        }
    }

    /**
     * Sets the current photo as the default photo used as preview for knittings list
     */
    private fun setDefaultPhoto() {
        photo?.let {
            val knitting = datasource.getKnitting(it.knittingID)
            datasource.updateKnitting(knitting.copy(defaultPhoto = it))
            debug("Set $it as default photo")
            view?.let {
                Snackbar.make(it, "Used as main photo", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Saves the image as PNG to the app's cache directory.
     * @param image Bitmap to save.
     * @return Uri of the saved file or null
     */
    private fun saveImage(image: Bitmap): Uri? {
        //TODO - Should be processed in another thread
        var uri: Uri? = null
        context?.let {
            val imagesFolder = File(it.cacheDir, "images")
            try {
                imagesFolder.mkdirs()
                val file = File(imagesFolder, "shared_image.jpg")

                val stream = FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                stream.flush()
                stream.close()
                uri = FileProvider.getUriForFile(it, "com.mthaler.knittings.fileprovider", file)
            } catch (e: IOException) {
                error("IOException while trying to write file for sharing: " + e.message)
            }
        }
        return uri
    }

    /**
     * Shares the PNG image from Uri.
     * @param uri Uri of image to share.
     */
    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
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
