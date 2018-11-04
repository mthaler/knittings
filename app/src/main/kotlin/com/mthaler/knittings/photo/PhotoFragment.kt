package com.mthaler.knittings.photo

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
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
                val path = photo!!.filename.absolutePath
                doAsync {
                    val orientation = PictureUtils.getOrientation(path)
                    val scaled = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                    val rotated = PictureUtils.rotateBitmap(scaled, orientation)
                    uiThread {
                        imageView.setImageBitmap(rotated)
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
        inflater?.let { it.inflate(R.menu.photo, menu) }
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
                activity?.let { it.finish() }
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


    companion object {
        private const val EXTRA_PHOTO_ID = "com.mthaler.knittings.photo_id"

        fun newInstance(photoID: Long): PhotoFragment {
            val bundle = Bundle()
            bundle.putLong(EXTRA_PHOTO_ID, photoID)

            val fragment = PhotoFragment()
            fragment.arguments = bundle

            return fragment
        }
    }
}
