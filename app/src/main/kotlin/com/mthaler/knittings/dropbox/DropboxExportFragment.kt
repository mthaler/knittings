package com.mthaler.knittings.dropbox

import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.mthaler.knittings.R
import kotlinx.android.synthetic.main.fragment_dropbox_export.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.uiThread

class DropboxExportFragment : AbstractDropboxFragment() {

    private var exportTask: AsyncTask<Any, Int?, Any?>? = null
    private var exporting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        setRetainInstance(true)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dropbox_export, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            login_button.visibility = View.GONE
            email_text.visibility = View.VISIBLE
            name_text.visibility = View.VISIBLE
            type_text.visibility = View.VISIBLE
            export_button.isEnabled = true
            setMode(exporting)
        } else {
            login_button.visibility = View.VISIBLE
            email_text.visibility = View.GONE
            name_text.visibility = View.GONE
            type_text.visibility = View.GONE
            export_button.isEnabled = false
        }
    }

    override fun loadData() {
        doAsync {
            val client = DropboxClientFactory.getClient()
            val account = client.users().currentAccount
            val spaceUsage = client.users().spaceUsage
            uiThread {
                email_text.text = account.email
                name_text.text = account.name.displayName
                type_text.text = account.accountType.name
                max_space_text.text = "Max: " + humanReadableByteCount(spaceUsage.allocation.individualValue.allocated)
                used_space_text.text = "Used: " + humanReadableByteCount(spaceUsage.used)
                free_space_text.text = "Free: " + humanReadableByteCount(spaceUsage.allocation.individualValue.allocated - spaceUsage.used)
            }
        }
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
        this.exporting = exporting
    }

    companion object {
        fun humanReadableByteCount(bytes: Long): String {
            val unit = 1024
            if (bytes < unit) return bytes.toString() + " B"
            val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
            val pre = "KMGTPE"[exp - 1]
            return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
        }
    }
}
