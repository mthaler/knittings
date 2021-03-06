package com.mthaler.knittings.needle

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mthaler.knittings.databinding.ListItemNeedleBinding
import com.mthaler.knittings.databinding.ListItemNeedleHeaderBinding
import com.mthaler.knittings.model.Needle
import com.mthaler.knittings.model.NeedleMaterial
import com.mthaler.knittings.model.NeedleType
import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class NeedleAdapter(private val onItemClick: (Needle) -> Unit, private val onItemLongLick: (Needle) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = ArrayList<ListItem>()

    fun setItems(items: List<ListItem>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            TypeHeader -> {
                val binding = ListItemNeedleHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return HeaderViewHolder(binding)
            }
            TypeItem -> {
                val binding = ListItemNeedleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.bind((items[position] as HeaderItem).header)
        } else if (holder is ItemViewHolder) {
            holder.bind((items[position] as NeedleItem).needle, onItemClick, onItemLongLick)
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].getType()

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(val binding: ListItemNeedleHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: String) {
            binding.needleListItemHeader.text = header
        }
    }

    class ItemViewHolder(val binding: ListItemNeedleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(needle: Needle, onItemClick: (Needle) -> Unit, onItemLongLick: (Needle) -> Unit) {
            val sb = StringBuilder()
            sb.append(NeedleMaterial.format(itemView.context, needle.material))
            sb.append("  ")
            if (needle.length.trim().isNotEmpty()) {
                sb.append("L ")
                sb.append(needle.length)
                sb.append("  ")
            }
            if (needle.size.trim().isNotEmpty()) {
                sb.append("\u00D8 ")
                sb.append(needle.size)
                sb.append("  ")
            }
            if (needle.inUse) {
                sb.append("  \u2713")
            }
            binding.name.text = if (needle.name.trim().isNotEmpty()) needle.name.trim() else NeedleType.format(itemView.context, needle.type)
            binding.description.text = sb.toString()
            itemView.setOnClickListener { onItemClick(needle) }
            itemView.setOnLongClickListener { onItemLongLick(needle); true }
        }
    }

    abstract class ListItem {

        abstract fun getType(): Int
    }

    data class HeaderItem(val header: String) : ListItem() {

        override fun getType(): Int = TypeHeader
    }

    data class NeedleItem(val needle: Needle) : ListItem() {

        override fun getType(): Int = TypeItem
    }

    companion object {
        val TypeHeader = 0
        val TypeItem = 1

        fun groupItems(context: Context, needles: List<Needle>): List<ListItem> {
            // group needles by type
            val grouped = needles.groupBy { it.type }
            val result = ArrayList<ListItem>()
            for (group in grouped) {
                result.add(HeaderItem(NeedleType.format(context, group.key)))
                for (needle in group.value) {
                    result.add(NeedleItem(needle))
                }
            }
            return result
        }
    }
}