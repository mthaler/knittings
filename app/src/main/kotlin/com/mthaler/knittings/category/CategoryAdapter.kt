package com.mthaler.knittings.category

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Category

class CategoryAdapter(val categories: ArrayList<Category>,
                      private val onItemClick: (Category) -> Unit,
                      private val onItemLongClick: (Category) -> Unit): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    /**
     * Creates, configures and returns a ViewHolder object for a particular row in the list
     *
     * @param parent a ViewGroup that will hold the views managed by the holder, mostly used for layout inflation
     * @param viewType an int that is the particular view type we are using, for cases where we have multiple view types
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(v)
    }

    /**
     * Updates the ViewHolder based upon the model data for a certain position
     *
     * @param holder ViewHolder object that should be updated
     * @param position model position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position], onItemClick, onItemLongClick)
    }

    /**
     * Returns the number of items in the recycler view
     */
    override fun getItemCount(): Int = categories.size

    /**
     * The ViewHolder class is responsible for binding data as needed from our model into the widgets
     * for a row in our list
     *
     * @param itemView item view
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
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