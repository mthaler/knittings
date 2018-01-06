package com.mthaler.knittings

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.EditText
import android.widget.ImageView
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.model.Photo
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * PhotoFragment displays a photo and the description
 */
class PhotoFragment : Fragment(), PhotoDetailsView {

    override var photo: Photo? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_photo, parent, false)

        val editTextDescription = v.findViewById<EditText>(R.id.photo_description)
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

    override fun init(photo: Photo) {
        val v = view
        if (v != null) {
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
            editTextDescription.setText(photo.description)
        }
        this.photo = photo
    }

    override fun deletePhoto() {
        // check if the photo is used as default photo
        val p = photo
        if (p != null) {
            val knitting = datasource.getKnitting(p.knittingID)
            if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == p.id) {
                datasource.updateKnitting(knitting.copy(defaultPhoto = null))
            }
            // delete database entry
            datasource.deletePhoto(p)
            photo = null
        } else {
            error("Cannot delete photo because it is null")
        }
    }
}
