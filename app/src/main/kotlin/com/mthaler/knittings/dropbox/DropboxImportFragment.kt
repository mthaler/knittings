package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mthaler.knittings.R
import org.jetbrains.anko.AnkoLogger

class DropboxImportFragment : AbstractDropboxFragment(), AnkoLogger {

    private var importTask: AsyncTask<Any, Int?, Any?>? = null
    private var importing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dropbox_import, container, false)
    }

    override fun loadData() {

    }

    companion object {
        private val AppKey = "6ybf7tgqdbhf641"
    }
}
