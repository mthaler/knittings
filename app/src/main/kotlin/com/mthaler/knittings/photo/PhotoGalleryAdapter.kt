package com.mthaler.knittings.photo

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.dbapp.model.Photo
import com.mthaler.dbapp.utils.PictureUtils
import com.mthaler.knittings.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoGalleryAdapter(context: Context, private val lifecycleScope: LifecycleCoroutineScope, private val onItemClick: (Photo) -> Unit) : RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder>() {

    private val photos = ArrayList<Photo>()
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val displayPhotoSize = sharedPref.getBoolean("display_photo_size", false)

    fun setPhotos(photos: List<Photo>) {
        this.photos.clear()
        this.photos.addAll(photos)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = photos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var title: TextView = view.findViewById(R.id.text)
        var image: ImageView = view.findViewById(R.id.image)

        init {
            view.setOnClickListener { v -> onItemClick(photos[adapterPosition]) }
        }

        fun bind(photo: Photo) {
            title.visibility = View.INVISIBLE
            val viewTreeObserver = image.viewTreeObserver
            viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    image.viewTreeObserver.removeOnPreDrawListener(this)
                    val width = image.measuredWidth
                    val height = image.measuredHeight
                    val filename = photo.filename.absolutePath
                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.Default) {
                            val file = File(filename)
                            if (file.exists()) {
                                val imageSize = if (displayPhotoSize) File(filename).length() else 0L
                                val orientation = PictureUtils.getOrientation(filename)
                                val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                                val rotatedPhoto = PictureUtils.rotateBitmap(photo, orientation)
                                Pair(imageSize, rotatedPhoto)
                            } else {
                                null
                            }
                        }
                        if (result != null) {
                            val (imageSize, rotatedPhoto) = result
                            image.setImageBitmap(rotatedPhoto)
                            if (displayPhotoSize) {
                                if (photo.description.isNotEmpty()) {
                                    title.visibility = View.VISIBLE
                                    title.text = photo.description + " (" + imageSize / 1024 + " KB)"
                                } else {
                                    title.visibility = View.VISIBLE
                                    title.text = "" + imageSize / 1024 + " KB"
                                }
                            } else {
                                if (photo.description.isNotEmpty()) {
                                    title.visibility = View.VISIBLE
                                    title.text = photo.description
                                }
                            }
                        } else {
                            image.setImageResource(R.drawable.ic_insert_photo_black_48dp)
                        }
                    }
                    return true
                }
            })
        }
    }
}