package com.mthaler.knittings.category

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category

class CategoryAdapter(val context: Context, val categories: ArrayList<Category>, val listener: OnItemClickListener): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(context, v)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class ViewHolder(val context: Context, itemView: View): RecyclerView.ViewHolder(itemView){
        val colorSwatch = itemView.findViewById<ColorSwatch>(R.id.category_list_item_color)
        val textFieldName = itemView.findViewById<TextView>(R.id.category_list_item_name)

        fun bind(category: Category, listener: OnItemClickListener) {
            val c = category.color
            if (c != null) {
                colorSwatch.color = c
            }
            textFieldName.text = category.name
            itemView.setOnClickListener({v -> listener.onItemClick(category) })
        }
    }

}