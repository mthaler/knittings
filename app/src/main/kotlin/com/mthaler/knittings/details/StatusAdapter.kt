package com.mthaler.knittings.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Status
import kotlinx.android.synthetic.main.spinner_item_status.view.*

class StatusAdapter(context: Context, statusList: List<Status>) : ArrayAdapter<Status>(context, 0, statusList) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }

    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val status = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_status, parent, false)
        view.statusImage.setImageResource(Status.getDrawableResource(context, status))
        view.statusText.text = Status.format(context, status)
        return view

    }
}