package com.mthaler.knittings.photo

import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo
import com.mthaler.knittings.utils.PictureUtils
import com.mthaler.knittings.utils.getOrientation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PhotoGalleryAdapter(private val context: Context, private val lifecycleScope: LifecycleCoroutineScope, private val onItemClick: (Photo) -> Unit) : RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder>() {

    private val photos = ArrayList<Photo>()
    private val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    private val displayPhotoSize = sharedPref.getBoolean("display_photo_size", false)

    fun setPhotos(photos: List<Photo>) {
        Log.w(TAG, Thread.currentThread().name)
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
            view.setOnClickListener { onItemClick(photos[bindingAdapterPosition]) }
        }

        fun bind(photo: Photo) {
            title.visibility = View.INVISIBLE
            updateImageView(image, title, photo, 50)
        }

        private fun updateImageView(imageView: ImageView, titleView: TextView, photo: Photo, delayMillis: Long) {
            if (delayMillis < 5000) {
                val viewTreeObserver = imageView.viewTreeObserver
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        imageView.viewTreeObserver.removeOnPreDrawListener(this)
                        val width = imageView.measuredWidth
                        val height = imageView.measuredHeight
                        if (width > 0 && height > 0) {
                            lifecycleScope.launch {
                                val result = withContext(Dispatchers.Default) {
                                    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                                    if (storageDir != null) {
                                        val f = File(storageDir, photo.filename.name)
                                        if (f.exists()) {
                                            val imageSize = if (displayPhotoSize) f.length() else 0L
                                            val orientation = f.toUri().getOrientation(imageView.context)
                                            val p = PictureUtils.decodeSampledBitmapFromPath(f.absolutePath, width, height)
                                            val rotatedPhoto = PictureUtils.rotateBitmap(p, orientation)
                                            Pair(imageSize, rotatedPhoto)
                                        } else {
                                            throw IllegalArgumentException("Photo $photo does not exist")
                                        }
                                    } else {
                                        throw IllegalArgumentException("Storage dir null")
                                    }

                                }
                                val (imageSize, rotatedPhoto) = result
                                imageView.setImageBitmap(rotatedPhoto)
                                if (displayPhotoSize) {
                                    if (photo.description.isNotEmpty()) {
                                        titleView.visibility = View.VISIBLE
                                        titleView.text = photo.description + " (" + imageSize / 1024 + " KB)"
                                    } else {
                                        titleView.visibility = View.VISIBLE
                                        titleView.text = "" + imageSize / 1024 + " KB"
                                    }
                                } else {
                                    if (photo.description.isNotEmpty()) {
                                        titleView.visibility = View.VISIBLE
                                        titleView.text = photo.description
                                    }
                                }
                            }
                        } else {
                            val handler = Handler(Looper.myLooper()!!)
                            handler.postDelayed({
                                updateImageView(imageView, titleView, photo, delayMillis * 2)
                            }, delayMillis)
                        }
                        return true
                    }
                })
            }
        }
    }
    companion object {
        val TAG = "PhotoGalleryAdapter"
    }
}