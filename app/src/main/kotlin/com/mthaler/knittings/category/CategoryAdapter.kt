package com.mthaler.knittings.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.databinding.ListItemCategoryBinding
import com.mthaler.knittings.model.Category


class CategoryAdapter(
    private val onItemClick: (Category) -> Unit,
    private val onItemLongClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val categories = ArrayList<Category>()

    fun setCategories(categories: List<Category>) {
        this.categories.clear()
        this.categories.addAll(categories)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], onItemClick, onItemLongClick)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(val binding: ListItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, onItemClick: (Category) -> Unit, onItemLongClick: (Category) -> Unit) {
            category.color?.let { binding.color.color = it }
            binding.name.text = category.name
            itemView.setOnClickListener { onItemClick(category) }
            itemView.setOnLongClickListener { onItemLongClick(category); true }
        }
    }
}