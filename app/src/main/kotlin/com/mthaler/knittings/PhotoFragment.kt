package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Photo
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * PhotoFragment displays a photo and the description
 */
class PhotoFragment : Fragment(), PhotoDetailsView {

    override var photo: Photo? = null

    private lateinit var imageView: ImageView
    private lateinit var editTextDescription: EditText

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo, parent, false)

        imageView = v.findViewById(R.id.image)

        // initialize description edit text
        editTextDescription = v.findViewById(R.id.photo_description)
        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(c: CharSequence, start: Int, before: Int, count: Int) {
                photo = photo?.copy(description = c.toString())
            }

            override fun beforeTextChanged(c: CharSequence, start: Int, count: Int, after: Int) {
                // this space intentionally left blank
            }

            override fun afterTextChanged(c: Editable) {
                // this one too
            }
        })

        return v
    }

    override fun onPause() {
        super.onPause()
        // we update the photo in the database when onPause is called
        // this is the case when the activity is party hidden or if an other activity is started
        if (photo != null) datasource.updatePhoto(photo!!)
    }

    override fun init(photo: Photo) {
        this.photo = photo
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        val viewTreeObserver = imageView.viewTreeObserver
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                imageView.viewTreeObserver.removeOnPreDrawListener(this)
                val width = imageView.measuredWidth
                val height = imageView.measuredHeight
                // loading and scaling the bitmap is expensive, use async task to do the work
                val path = photo.filename.absolutePath
                doAsync {
                    val orientation = PictureUtils.getOrientation(path)
                    val scaled = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                    val rotated = PictureUtils.rotateBitmap(scaled, orientation)
                    uiThread {
                        if (rotated != null) {
                            imageView.setImageBitmap(rotated)
                        }
                    }
                }
                return true
            }
        })
        editTextDescription.setText(photo.description)
    }

    override fun deletePhoto() {
        // check if the photo is used as default photo
        val knitting = datasource.getKnitting(photo!!.knittingID)
        if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == photo!!.id) {
            datasource.updateKnitting(knitting.copy(defaultPhoto = null))
        }
        // delete database entry
        datasource.deletePhoto(photo!!)
        photo = null
    }
}
