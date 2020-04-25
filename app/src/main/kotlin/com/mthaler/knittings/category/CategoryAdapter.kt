package com.mthaler.knittings.category

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
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
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], onItemClick, onItemLongClick)
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorSwatch = itemView.findViewById<ColorSwatch>(R.id.category_list_item_color)
        private val textFieldName = itemView.findViewById<TextView>(R.id.category_list_item_name)

        fun bind(category: Category, onItemClick: (Category) -> Unit, onItemLongClick: (Category) -> Unit) {
            val c = category.color
            if (c != null) {
                colorSwatch.color = c
            }
            textFieldName.text = category.name
            itemView.setOnClickListener { v -> onItemClick(category) }
            itemView.setOnLongClickListener { v -> onItemLongClick(category); true }
        }
    }
}