package com.mthaler.knittings.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoAdapter(val context: Context, private val lifecycleScope: LifecycleCoroutineScope) : RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    private val photos = ArrayList<Photo>()

    fun setPhotos(photos: List<Photo>) {
        this.photos.clear()
        this.photos.addAll(photos)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.pager_item_photo, parent, false)
        return this.ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView = itemView.findViewById<ImageView>(R.id.photo)

        fun bind(photo: Photo) {
            val viewTreeObserver = imageView.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = imageView.measuredWidth
                    val height = imageView.measuredHeight
                    val filename = photo.filename.absolutePath
                    lifecycleScope.launch {
                        val rotatedPhoto = withContext(Dispatchers.IO) {
                            val orientation = PictureUtils.getOrientation(filename)
                            val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                            PictureUtils.rotateBitmap(photo, orientation)
                        }
                        imageView.setImageBitmap(rotatedPhoto)
                    }
                    return true
                }
            })
        }
    }
}
