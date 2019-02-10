package com.mthaler.knittings.needle

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Needle
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.util.*

class NeedleAdapter(val needles: ArrayList<Needle>, private val onItemClick: (Needle) -> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Creates, configures and returns a ViewHolder object for a particular row in the list
     *
     * @param parent a ViewGroup that will hold the views managed by the holder, mostly used for layout inflation
     * @param viewType an int that is the particular view type we are using, for cases where we have multiple view types
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_needle, parent, false)
        if (viewType == TypeHeader) {
            return NeedleAdapter.HeaderViewHolder(v)
        } else if (viewType == TypeItem) {
            return NeedleAdapter.ItemViewHolder(v)
        } else {
            throw IllegalArgumentException("Unknown view type " + viewType)
        }
    }

    /**
     * Updates the ViewHolder based upon the model data for a certain position
     *
     * @param holder ViewHolder object that should be updated
     * @param position model position
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind("test")
        } else if (holder is ItemViewHolder) {
            holder.bind(needles[position], onItemClick)
        }
    }

    /**
     * Returns the number of items in the recycler view
     */
    override fun getItemCount(): Int = needles.size

    class HeaderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val textViewHeader = itemView.findViewById<TextView>(R.id.needle_list_item_header)

        fun bind(header: String) {
            textViewHeader.text = header
        }
    }

    /**
     * The ViewHolder class is responsible for binding data as needed from our model into the widgets
     * for a row in our list
     *
     * @param itemView item view
     */
    class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val textViewName = itemView.findViewById<TextView>(R.id.needle_list_item_name)
        private val textViewDescription= itemView.findViewById<TextView>(R.id.needle_list_item_description)

        fun bind(needle: Needle, listener: (Needle) -> Unit) {
            val sb = StringBuilder()
            if (!needle.length.trim().isEmpty()) {
                sb.append("L ")
                sb.append(needle.length)
                sb.append("  ")
            }
            if (!needle.size.trim().isEmpty()) {
                sb.append("\u2300 ")
                sb.append(needle.size)
                sb.append("  ")
            }
            if (needle.inUse) {
                sb.append("  \u2713")
            }
            textViewName.text = if (!needle.name.trim().isEmpty()) needle.name.trim() else needle.type.trim()
            textViewDescription.text = sb.toString()
            itemView.setOnClickListener { v -> listener(needle) }
        }
    }

    abstract class ListItem {

        abstract fun getType(): Int
    }

    data class HeaderItem(val header: String): ListItem() {

        override fun getType(): Int = TypeHeader
    }

    data class NeedleItem(val needle: Needle): ListItem() {

        override fun getType(): Int = TypeItem
    }

    companion object {
        val TypeHeader = 0
        val TypeItem = 1

        fun groupItems(needles: List<Needle>): List<ListItem> {
            // group needles by type
            val grouped = needles.groupBy { it.type }
            val result = ArrayList<ListItem>()
            for (group in grouped) {
                result.add(HeaderItem(group.key))
                for (needle in group.value) {
                    result.add(NeedleItem(needle))
                }
            }
            return result
        }
    }
}