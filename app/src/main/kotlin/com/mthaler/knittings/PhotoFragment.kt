package com.mthaler.knittings

import android.graphics.Bitmap
import android.os.AsyncTask
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
        if (photo != null) KnittingsDataSource.getInstance(activity).updatePhoto(photo!!)
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
                val scaleBitmapTask = object : AsyncTask<String, Void, Bitmap>() {
                    override fun doInBackground(vararg args: String): Bitmap {
                        val path = args[0]
                        val orientation = PictureUtils.getOrientation(path)
                        val photo = PictureUtils.decodeSampledBitmapFromPath(path, width, height)
                        return PictureUtils.rotateBitmap(photo, orientation)
                    }

                    override fun onPostExecute(bitmap: Bitmap?) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap)
                        }
                    }
                }
                scaleBitmapTask.execute(this@PhotoFragment.photo!!.filename.absolutePath)
                return true
            }
        })
        editTextDescription.setText(photo.description)
    }

    override fun deletePhoto() {
        // check if the photo is used as default photo
        val knitting = KnittingsDataSource.getInstance(activity).getKnitting(photo!!.knittingID)
        if (knitting.defaultPhoto != null && knitting.defaultPhoto.id == photo!!.id) {
            KnittingsDataSource.getInstance(activity).updateKnitting(knitting.copy(defaultPhoto = null))
        }
        // delete database entry
        KnittingsDataSource.getInstance(activity).deletePhoto(photo!!)
        photo = null
    }
}
