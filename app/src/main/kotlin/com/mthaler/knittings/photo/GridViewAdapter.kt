package com.mthaler.knittings.photo

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * An adapter that creates the views to display photos in a grid view
 */
class GridViewAdapter(context: Context, private val lifecycleScope: LifecycleCoroutineScope, private val layoutResourceId: Int, private val data: List<Photo>) : ArrayAdapter<Photo>(context, layoutResourceId, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var row: View? = convertView
        val h: ViewHolder

        if (row == null) {
            val inflater = (context as Activity).layoutInflater
            row = inflater.inflate(layoutResourceId, parent, false)
            h = ViewHolder()
            h.imageTitle = row.findViewById(R.id.text)
            h.image = row.findViewById(R.id.image)
            row.tag = h
        } else {
            h = row.tag as ViewHolder
        }

        val item = data[position]
        // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
        val imageView = h.image
        val imageTitle = h.imageTitle
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val displayPhotoSize = sharedPref.getBoolean("display_photo_size", false)
        if (imageTitle != null) {
            imageTitle.visibility = View.INVISIBLE
        }
        if (imageView != null) {
            val viewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = imageView.measuredWidth
                    val height = imageView.measuredHeight
                    val filename = item.filename.absolutePath
                    lifecycleScope.launch {
                        val (imageSize, rotatedPhoto) = withContext(Dispatchers.Default) {
                            val imageSize = if (displayPhotoSize) File(filename).length() else 0L
                            val orientation = PictureUtils.getOrientation(filename)
                            val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                            val rotatedPhoto = PictureUtils.rotateBitmap(photo, orientation)
                            Pair(imageSize, rotatedPhoto)
                        }
                        imageView.setImageBitmap(rotatedPhoto)
                        if (imageTitle != null) {
                            if (displayPhotoSize) {
                                if (item.description.isNotEmpty()) {
                                    imageTitle.visibility = View.VISIBLE
                                    imageTitle.text = item.description + " (" + imageSize / 1024 + " KB)"
                                } else {
                                    imageTitle.visibility = View.VISIBLE
                                    imageTitle.text = "" + imageSize / 1024 + " KB"
                                }
                            } else {
                                if (item.description.isNotEmpty()) {
                                    imageTitle.visibility = View.VISIBLE
                                    imageTitle.text = item.description
                                }
                            }
                        }
                    }
                    return true
                }
            })
        }

        return row!!
    }

    internal class ViewHolder {
        var imageTitle: TextView? = null
        var image: ImageView? = null
    }
}