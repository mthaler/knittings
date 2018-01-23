package com.mthaler.knittings.dropbox

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.activity_dropbox_export.*
import org.jetbrains.anko.support.v4.alert

class DropboxExportFragment : Fragment() {

    private var exportTask: AsyncTask<Any, Int?, Any?>? = null

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = inflater.inflate(R.layout.fragment_dropbox_export, parent, false)

        login_button.setOnClickListener { Auth.startOAuth2Authentication(context, getString(R.string.APP_KEY)) }

        export_button.setOnClickListener {
            val cm = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isWiFi = activeNetwork.type == ConnectivityManager.TYPE_WIFI
            if (!isWiFi) {
                alert {
                    title = resources.getString(R.string.dropbox_export)
                    message = resources.getString(R.string.dropbox_export_no_wifi_question)
                    positiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) {
                        exportTask = UploadTask(DropboxClientFactory.getClient(), context!!.applicationContext, progressBar::setProgress, ::setMode).execute()
                        setMode(true)
                    }
                    negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                }.show()
            } else  {
                exportTask = UploadTask(DropboxClientFactory.getClient(), context!!.applicationContext, progressBar::setProgress, ::setMode).execute()
                setMode(true)
            }
        }

        cancel_button.setOnClickListener { exportTask?.cancel(true) }

        return v;
    }

    private fun setMode(exporting: Boolean) {
        if (exporting) {
            export_button.visibility = View.GONE
            export_text.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            cancel_button.visibility = View.VISIBLE
        } else {
            export_button.visibility = View.VISIBLE
            export_text.visibility = View.GONE
            progressBar.visibility = View.GONE
            progressBar.progress = 0
            cancel_button.visibility = View.GONE
        }
    }
}
