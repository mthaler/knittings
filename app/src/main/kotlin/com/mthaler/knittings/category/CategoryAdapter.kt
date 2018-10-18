package com.mthaler.knittings.category

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category

class CategoryAdapter(val categories: ArrayList<Category>): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtName?.text = categories[position].name
        holder.txtTitle?.text = categories[position].color.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtName = itemView.findViewById<TextView>(R.id.name)
        val txtTitle = itemView.findViewById<TextView>(R.id.color)

    }

}