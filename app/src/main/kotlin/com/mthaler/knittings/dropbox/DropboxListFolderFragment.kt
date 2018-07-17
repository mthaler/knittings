package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.mthaler.knittings.R
import org.jetbrains.anko.AnkoLogger

class DropboxListFolderFragment : ListFragment(), AnkoLogger {

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        Toast.makeText(context, "Import", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val items = it.getStringArray(ARG_ITEMS)
            val adapter = ListFolderAdapter(items)
            listAdapter = adapter
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dropbox_list_folder, container, false)
    }

    companion object {

        val ARG_ITEMS = "items"

        @JvmStatic
        fun newInstance(items: Array<String>) =
                DropboxListFolderFragment().apply {
                    arguments = Bundle().apply {
                        putStringArray(ARG_ITEMS, items)
                    }
                }
    }

    private inner class ListFolderAdapter(items: Array<String>) : ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            // if we weren't given a view, inflate one
            if (null == convertView) {
                convertView = activity!!.layoutInflater
                        .inflate(R.layout.list_item_folder, null)
            }

            val item = getItem(position)

            val titleTextView = convertView!!.findViewById<TextView>(R.id.list_item_folder_name)
            titleTextView.text = item

            return convertView
        }
    }
}
