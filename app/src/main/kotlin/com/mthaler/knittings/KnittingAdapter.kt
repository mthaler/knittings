package com.mthaler.knittings

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mthaler.knittings.model.Knitting
import java.text.DateFormat
import com.mthaler.knittings.model.Status.*

class KnittingAdapter(val context: Context, val knittings: List<Knitting>, private val listener: OnItemClickListener): RecyclerView.Adapter<KnittingAdapter.ViewHolder>() {

    /**
     * Creates, configures and returns a ViewHolder object for a particular row in the list
     *
     * @param parent a ViewGroup that will hold the views managed by the holder, mostly used for layout inflation
     * @param viewType an int that is the particular view type we are using, for cases where we have multiple view types
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_knitting, parent, false)
        return ViewHolder(context, v)
    }

    /**
     * Updates the ViewHolder based upon the model data for a certain position
     *
     * @param holder ViewHolder object that should be updated
     * @param position model position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(knittings[position])
    }

    /**
     * Returns the number of items in the recycler view
     */
    override fun getItemCount(): Int = knittings.size

    /**
     * The ViewHolder class is responsible for binding data as needed from our model into the widgets
     * for a row in our list
     *
     * @param context context
     * @param itemView item view
     */
    inner class ViewHolder(val context: Context, itemView: View): RecyclerView.ViewHolder(itemView){
        private val titleTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_titleTextView)
        private val descriptionTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_descriptionTextView)
        private val startedTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_startedTextView)
        private val photoView = itemView.findViewById<ImageView>(R.id.knitting_list_item_photoImageView)
        private val categoryIndicator = itemView.findViewById<CategoryIndicator>(R.id.knitting_list_item_categoryIndicator)
        private val statusImageView = itemView.findViewById<ImageView>(R.id.knitting_list_item_statusImageView)

        init {
            itemView.setOnClickListener { v -> listener.onItemClick(knittings[adapterPosition] ) }
        }

        fun bind(knitting: Knitting) {
            titleTextView.text = knitting.title
            descriptionTextView.text = knitting.description
            startedTextView.text = DateFormat.getDateInstance().format(knitting.started)
            // the list item views are reused, we always need to set bitmap, otherwise the previous bitmap is used
            if (knitting.defaultPhoto?.preview != null) {
                photoView.setImageBitmap(knitting.defaultPhoto.preview)
            } else {
                photoView.setImageBitmap(null)
            }
            if (knitting.category != null) {
                if (knitting.category.color != null) {
                    categoryIndicator.color = knitting.category.color
                } else {
                    categoryIndicator.color = Color.WHITE
                }
            } else {
                categoryIndicator.color = Color.WHITE
            }
            when(knitting.status) {
                IN_THE_WORKS -> statusImageView.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                FINISHED -> statusImageView.setImageResource(R.drawable.ic_check_circle_outline_24px)
                PLANNED -> statusImageView.setImageResource(R.drawable.ic_outline_assignment_24px)
                ON_HOLD -> statusImageView.setImageResource(R.drawable.ic_pause_circle_outline_black_24dp)
                DISCARDED -> statusImageView.setImageResource(R.drawable.ic_highlight_off_black_24dp)
            }
        }
    }

}