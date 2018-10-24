package com.mthaler.knittings.category

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
        val imgColor = itemView.findViewById<ImageView>(R.id.category_list_item_color)
        val txtName = itemView.findViewById<TextView>(R.id.category_list_item_name)

        fun bind(category: Category, listener: OnItemClickListener) {
            val drawable = context.getResources().getDrawable(R.drawable.color_picker_swatch)
            if (category.color != null) {
                drawable.setColorFilter(category.color, PorterDuff.Mode.SRC_ATOP)
            }
            imgColor.setImageDrawable(drawable)
            txtName.text = category.name
            itemView.setOnClickListener({v -> listener.onItemClick(category) })
        }
    }

}