package com.mthaler.knittings

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mthaler.knittings.databinding.ListItemKnittingBinding
import com.mthaler.knittings.model.Knitting
import com.mthaler.knittings.model.Status
import java.text.DateFormat

class KnittingAdapter(
    val context: Context,
    private val onItemClick: (Knitting) -> Unit,
    private val onItemLongClick: (Knitting) -> Unit
) : RecyclerView.Adapter<KnittingAdapter.ViewHolder>() {

    private var knittings = ArrayList<Knitting>()

    fun setKnittings(knittings: List<Knitting>) {
        this.knittings.clear()
        this.knittings.addAll(knittings)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemKnittingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(context, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(knittings[position])
    }

    override fun getItemCount(): Int = knittings.size

    inner class ViewHolder(val context: Context, val binding: ListItemKnittingBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { v -> onItemClick(knittings[adapterPosition]) }
            itemView.setOnLongClickListener { v -> onItemLongClick(knittings[adapterPosition]); true }
        }

        fun bind(knitting: Knitting) {
            binding.knittingListItemTitleTextView.text = knitting.title
            binding.knittingListItemDescriptionTextView.text = knitting.description
            binding.knittingListItemStartedTextView.text = DateFormat.getDateInstance().format(knitting.started)
            // the list item views are reused, we always need to set bitmap, otherwise the previous bitmap is used
            if (knitting.defaultPhoto?.preview != null) {
                binding.knittingListItemPhotoImageView.setImageBitmap(knitting.defaultPhoto.preview)
            } else {
                binding.knittingListItemPhotoImageView.setImageResource(R.drawable.categories)
            }
            if (knitting.category != null) {
                val c = knitting.category.color
                if (c != null) {
                    binding.knittingListItemCategoryIndicator.color = c
                } else {
                    binding.knittingListItemCategoryIndicator.color = Color.WHITE
                }
            } else {
                binding.knittingListItemCategoryIndicator.color = Color.WHITE
            }
            binding.knittingListItemStatusImageView.setImageResource(Status.getDrawableResource(context, knitting.status))
        }
    }
}