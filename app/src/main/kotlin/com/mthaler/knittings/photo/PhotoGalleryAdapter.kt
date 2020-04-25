package com.mthaler.knittings.photo

import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo

abstract class PhotoGalleryAdapter(val context: Context,
                                   private val onItemClick: (Photo) -> Unit,
                                   private val onItemLongClick: (Photo) -> Unit) : RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder>() {

    private val photos = ArrayList<Photo>()

    fun setPhotos(photos: List<Photo>) {
        this.photos.clear()
        this.photos.addAll(photos)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = photos.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.grid_item_layout, parent, false)
        return ViewHolder(context, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(context: Context, view: View) : RecyclerView.ViewHolder(view) {

        var imageTitle: TextView = view.findViewById(R.id.text)
        var image: ImageView = view.findViewById(R.id.image)

        init {
            image.setOnClickListener { v -> onItemClick(photos[adapterPosition]) }
            image.setOnLongClickListener { v -> onItemLongClick(photos[adapterPosition]); true }
        }

        fun bind(photo: Photo) {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
            val displayPhotoSize = sharedPref.getBoolean("display_photo_size", false)
        }
    }
}