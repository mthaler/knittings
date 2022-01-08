package com.mthaler.knittings.photo

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.GridItemLayoutBinding
import com.mthaler.knittings.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Handler

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
        val binding = GridItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    inner class ViewHolder(val binding: GridItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { v -> onItemClick(photos[adapterPosition]) }
        }

        fun bind(photo: Photo) {
            //binding.text.visibility = View.INVISIBLE
            //updateImageView(binding.image, binding.text, photo, 50)
        }

        private fun updateImageView(imageView: ImageView, titleView: TextView, photo: Photo, delayMillis: Long, context: Context) {
            if (delayMillis < 5000) {
                val viewTreeObserver = imageView.viewTreeObserver
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        imageView.viewTreeObserver.removeOnPreDrawListener(this)
                        val width = imageView.measuredWidth
                        val height = imageView.measuredHeight
                        if (width > 0 && height > 0) {
                            val filename = photo.filename.absolutePath
                            /*lifecycleScope.launch {
                                val result = withContext(Dispatchers.Default) {
                                    val file = File(filename)
                                    if (file.exists()) {
                                        val imageSize = if (displayPhotoSize) File(filename).length() else 0L
                                        val orientation = PictureUtils.getOrientation(Uri.fromFile(File(filename)), context)
                                        val photo = PictureUtils.decodeSampledBitmapFromPath(filename, width, height)
                                        val rotatedPhoto = PictureUtils.rotateBitmap(photo, orientation)
                                        Pair(imageSize, rotatedPhoto)

                                    } else {
                                        null
                                    }
                                }
                                if (result != null) {
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
                                } else {
                                    imageView.setImageResource(R.drawable.ic_insert_photo_black_48dp)
                                }
                            }*/
                        } else {
                            /*val handler = Handler()
                            handler.postDelayed({
                                updateImageView(imageView, titleView, photo, delayMillis * 2, context)
                            }, delayMillis)*/
                        }
                        return true
                    }
                })
            }
        }
    }
}