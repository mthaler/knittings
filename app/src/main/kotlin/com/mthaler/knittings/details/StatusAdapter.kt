package com.mthaler.knittings.details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mthaler.knittings.R
import com.mthaler.knittings.model.Status

class StatusAdapter(context: Context) : ArrayAdapter<Status>(context, 0, Status.values()) {

    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val status = getItem(position)!!
        val view = recycledView ?: LayoutInflater.from(context).inflate(R.layout.spinner_status_selected, parent, false)
        val statusImage = view.findViewById<ImageView>(R.id.statusImage)
        statusImage.setImageResource(Status.getDrawableResource(status))
        val statusText = view.findViewById<TextView>(R.id.statusText)
        statusText.text = Status.format(context, status)
        return view
    }

    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val status = getItem(position)!!
        val view = recycledView ?: LayoutInflater.from(context).inflate(R.layout.spinner_status_row, parent, false)
        val statusImage = view.findViewById<ImageView>(R.id.statusImage)
        statusImage.setImageResource(Status.getDrawableResource(status))
        val statusText = view.findViewById<TextView>(R.id.statusText)
        statusText.text = Status.format(context, status)
        return view
    }
}