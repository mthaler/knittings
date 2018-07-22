package com.mthaler.knittings.dropbox

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.mthaler.knittings.R
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_dropbox_export.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.uiThread
import org.jetbrains.anko.wtf
import com.mthaler.knittings.utils.StringUtils.formatBytes

/**
 * Fragment used for Dropbox export
 */
class DropboxExportFragment : AbstractDropboxFragment(), AnkoLogger {

    private var exportTask: AsyncTask<Any, Int?, Any?>? = null
    private var exporting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_dropbox_export, parent, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener { Auth.startOAuth2Authentication(context, AppKey) }

        export_button.setOnClickListener {
            val ctx = context
            if (ctx != null) {
                val isWiFi = NetworkUtils.isWifiConnected(ctx)
                if (!isWiFi) {
                    alert {
                        title = resources.getString(R.string.dropbox_export)
                        message = resources.getString(R.string.dropbox_export_no_wifi_question)
                        positiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) {
                            exportTask = UploadTask(DropboxClientFactory.getClient(), ctx.applicationContext, progressBar::setProgress, ::setMode).execute()
                            setMode(true)
                        }
                        negativeButton(resources.getString(R.string.dialog_button_cancel)) {}
                    }.show()
                } else  {
                    exportTask = UploadTask(DropboxClientFactory.getClient(), ctx.applicationContext, progressBar::setProgress, ::setMode).execute()
                    setMode(true)
                }
            } else {
                wtf("context null")
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
                max_space_text.text = "Max: " + formatBytes(spaceUsage.allocation.individualValue.allocated)
                used_space_text.text = "Used: " + formatBytes(spaceUsage.used)
                free_space_text.text = "Free: " + formatBytes(spaceUsage.allocation.individualValue.allocated - spaceUsage.used)
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
        private val AppKey = "6ybf7tgqdbhf641"
    }
}
