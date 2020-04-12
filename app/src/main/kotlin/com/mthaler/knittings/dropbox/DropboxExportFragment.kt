package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.mthaler.knittings.R
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_dropbox_export.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.uiThread
import com.mthaler.knittings.utils.StringUtils.formatBytes

/**
 * Fragment used for Dropbox export
 */
class DropboxExportFragment : AbstractDropboxFragment() {

    private var exportTask: AsyncTask<Void, Int?, Any?>? = null
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
            context?.let {
                val isWiFi = NetworkUtils.isWifiConnected(it)
                if (!isWiFi) {
                    val builder = AlertDialog.Builder(it)
                    with (builder) {
                        setTitle(resources.getString(R.string.dropbox_export))
                        setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                        setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button), { dialog, which ->
                            exportTask = UploadTask(DropboxClientFactory.getClient(), it.applicationContext, progressBar::setProgress, ::onUploadComplete, ::onUploadError).execute()
                            setMode(true)
                        })
                        setNegativeButton(resources.getString(R.string.dialog_button_cancel), { dialog, which -> })
                        show()
                    }
                } else {
                    exportTask = UploadTask(DropboxClientFactory.getClient(), it.applicationContext, progressBar::setProgress, ::onUploadComplete, ::onUploadError).execute()
                    setMode(true)
                }
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

    override fun loadData(onError: (Exception) -> Unit) {
        doAsync {
            val client = DropboxClientFactory.getClient()
            var account: FullAccount? = null
            var spaceUsage: SpaceUsage? = null
            var exception: Exception? = null
            try {
                account = client.users().currentAccount
                spaceUsage = client.users().spaceUsage
            } catch (ex: Exception) {
                exception = ex
            }
            uiThread {
                if (exception != null) {
                    onError(exception)
                } else {
                    if (account != null) {
                        email_text.text = account.email
                        name_text.text = account.name.displayName
                        type_text.text = account.accountType.name
                    }
                    if (spaceUsage != null) {
                        max_space_text.text = "Max: " + formatBytes(spaceUsage.allocation.individualValue.allocated)
                        used_space_text.text = "Used: " + formatBytes(spaceUsage.used)
                        free_space_text.text = "Free: " + formatBytes(spaceUsage.allocation.individualValue.allocated - spaceUsage.used)
                    }
                }
            }
        }
    }

    private fun onUploadComplete(cancelled: Boolean) {
        setMode(false)
        if (cancelled) {
            alert {
                title = resources.getString(R.string.dropbox_export)
                message = "Dropbox export was cancelled"
                positiveButton("OK") {}
            }.show()
        } else {
            alert {
                title = resources.getString(R.string.dropbox_export)
                message = "Dropbox export completed"
                positiveButton("OK") {}
            }.show()
        }
    }

    private fun onUploadError(ex: Exception) {
        alert {
            title = resources.getString(R.string.dropbox_export)
            message = "Dropbox export failed: " + ex.message
            positiveButton("OK") {}
        }.show()
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
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
