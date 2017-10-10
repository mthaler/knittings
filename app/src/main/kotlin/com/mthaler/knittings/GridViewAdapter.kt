package com.mthaler.knittings

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mthaler.knittings.model.Photo

/**
 * An adapter that creates the views to display photos in a grid view
 */
class GridViewAdapter(context: Context, private val layoutResourceId: Int, private val data: List<Photo>) : ArrayAdapter<Photo>(context, layoutResourceId, data) {

    /**
     * Creates a view for the item at the given position in the list
     *
     * @param position position of the item in the list
     * @param convertView the old view to reuse, if possible
     * @param parent the parent that this view will eventually be attached to, never null
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var row: View? = convertView
        val h: ViewHolder

        if (row == null) {
            val inflater = (context as Activity).layoutInflater
            row = inflater.inflate(layoutResourceId, parent, false)
            h = ViewHolder()
            h.imageTitle = row!!.findViewById(R.id.text)
            h.image = row.findViewById(R.id.image)
            row.tag = h
        } else {
            h = row.tag as ViewHolder
        }

        if (position < count - 1) {
            val item = data[position]
            // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
            val viewTreeObserver = h.image!!.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    h.image!!.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = h.image!!.measuredWidth
                    val height = h.image!!.measuredHeight
                    val filename = item.filename.absolutePath
                    val orientation = PictureUtils.getOrientation(filename)
                    val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                    val rotatedPhoto = PictureUtils.rotateBitmap(photo, orientation)
                    h.image!!.setImageBitmap(rotatedPhoto)
                    h.imageTitle!!.text = item.description
                    return true
                }
            })
        } else {
            // we use a view tree observer to get the width and the height of the image view and scale the image accordingly reduce memory usage
            val viewTreeObserver = h.image!!.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    h.image!!.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = h.image!!.measuredWidth
                    val img = BitmapFactory.decodeResource(this@GridViewAdapter.context.resources, R.drawable.add_photo)
                    val scaled = PictureUtils.resize(img, width, width)
                    h.image!!.setImageBitmap(scaled)
                    return true
                }
            })
        }

        return row
    }

    override fun getCount(): Int {
        // add an additional element used to display add photo icon
        return super.getCount() + 1
    }

    internal class ViewHolder {
        var imageTitle: TextView? = null
        var image: ImageView? = null
    }
}

