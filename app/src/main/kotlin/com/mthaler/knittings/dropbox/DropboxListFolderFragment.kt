package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.R

class DropboxListFolderFragment : Fragment() {

    private var items: Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            items = it.getStringArray(ARG_ITEMS)
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
}
