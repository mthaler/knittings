package com.mthaler.knittings.photo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Photo
import com.squareup.picasso.Picasso

class PhotoGalleryAdapter(val context: Context, private val onItemClick: (Photo) -> Unit) : RecyclerView.Adapter<PhotoGalleryAdapter.ViewHolder>() {

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
        holder.bind(photos[position])
    }

    inner class ViewHolder(context: Context, view: View) : RecyclerView.ViewHolder(view) {

        var title: TextView = view.findViewById(R.id.text)
        var image: ImageView = view.findViewById(R.id.image)

        init { 
            view.setOnClickListener { v -> onItemClick(photos[adapterPosition]) }
        }

        fun bind(photo: Photo) {
            title.visibility = View.INVISIBLE
            if (photo.description.isNotEmpty()) {
                title.visibility = View.VISIBLE
                title.text = photo.description
            }
            Picasso.get().load(photo.filename).into(image);
        }
    }
}