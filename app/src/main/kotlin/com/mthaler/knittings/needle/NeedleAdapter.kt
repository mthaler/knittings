package com.mthaler.knittings.needle

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Needle

class NeedleAdapter(val needles: ArrayList<Needle>, private val onItemClick: (Needle) -> Unit): RecyclerView.Adapter<NeedleAdapter.ViewHolder>() {

    /**
     * Creates, configures and returns a ViewHolder object for a particular row in the list
     *
     * @param parent a ViewGroup that will hold the views managed by the holder, mostly used for layout inflation
     * @param viewType an int that is the particular view type we are using, for cases where we have multiple view types
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NeedleAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_needle, parent, false)
        return NeedleAdapter.ViewHolder(v)
    }

    /**
     * Updates the ViewHolder based upon the model data for a certain position
     *
     * @param holder ViewHolder object that should be updated
     * @param position model position
     */
    override fun onBindViewHolder(holder: NeedleAdapter.ViewHolder, position: Int) {
        holder.bind(needles[position], onItemClick)
    }

    /**
     * Returns the number of items in the recycler view
     */
    override fun getItemCount(): Int = needles.size


    /**
     * The ViewHolder class is responsible for binding data as needed from our model into the widgets
     * for a row in our list
     *
     * @param itemView item view
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val textFieldName = itemView.findViewById<TextView>(R.id.needle_list_item_name)

        fun bind(category: Needle, listener: (Needle) -> Unit) {
            textFieldName.text = category.name
            itemView.setOnClickListener { v -> listener(category) }
        }
    }
}