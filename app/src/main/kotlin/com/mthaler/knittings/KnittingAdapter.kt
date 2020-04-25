package com.mthaler.knittings

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import java.text.DateFormat

class KnittingAdapter(
    val context: Context,
    val knittings: List<Knitting>,
    private val onItemClick: (Knitting) -> Unit,
    private val onItemLongClick: (Knitting) -> Unit
) : RecyclerView.Adapter<KnittingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_knitting, parent, false)
        return ViewHolder(context, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(knittings[position])
    }

    override fun getItemCount(): Int = knittings.size

    inner class ViewHolder(val context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_titleTextView)
        private val descriptionTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_descriptionTextView)
        private val startedTextView = itemView.findViewById<TextView>(R.id.knitting_list_item_startedTextView)
        private val photoView = itemView.findViewById<ImageView>(R.id.knitting_list_item_photoImageView)
        private val categoryIndicator = itemView.findViewById<CategoryIndicator>(R.id.knitting_list_item_categoryIndicator)
        private val statusImageView = itemView.findViewById<ImageView>(R.id.knitting_list_item_statusImageView)

        init {
            itemView.setOnClickListener { v -> onItemClick(knittings[adapterPosition]) }
            itemView.setOnLongClickListener { v -> onItemLongClick(knittings[adapterPosition]); true }
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
            statusImageView.setImageResource(Status.getDrawableResource(context, knitting.status))
        }
    }
}