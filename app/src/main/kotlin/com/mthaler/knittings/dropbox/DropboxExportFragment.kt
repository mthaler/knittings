package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.mthaler.knittings.R
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_dropbox_export.*
import com.mthaler.knittings.utils.StringUtils.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Fragment used for Dropbox export
 */
class DropboxExportFragment : AbstractDropboxFragment() {

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
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_export))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { dialog, which ->
                        DropboxExportService.startService(requireContext())
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            } else {
                DropboxExportService.startService(requireContext())
            }
        }

        cancel_button.setOnClickListener { /*exportTask?.cancel(true)*/ }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sm = DropboxExportServiceManager.getInstance()

        sm.jobStatus.observe(viewLifecycleOwner, Observer { jobStatus ->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    export_button.isEnabled = true
                    export_title.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    cancel_button.visibility = View.GONE
                    result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    export_button.isEnabled = false
                    export_title.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    cancel_button.visibility = View.VISIBLE
                    result.visibility = View.GONE
                    progressBar.progress = jobStatus.value
                }
                is JobStatus.Success -> {
                    export_button.isEnabled = true
                    export_title.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    cancel_button.visibility = View.GONE
                    result.visibility = View.VISIBLE
                    result.text = "Dropbox export completed" //jobStatus.result
                }
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, Observer { serviceStatus ->
            when(serviceStatus) {
                ServiceStatus.Stopped -> export_button.isEnabled = true
                ServiceStatus.Started -> export_button.isEnabled = false
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            login_button.visibility = View.GONE
            account.visibility = View.VISIBLE
            email_text.visibility = View.VISIBLE
            name_text.visibility = View.VISIBLE
            type_text.visibility = View.VISIBLE
            export_button.isEnabled = true
        } else {
            login_button.visibility = View.VISIBLE
            account.visibility = View.GONE
            email_text.visibility = View.GONE
            name_text.visibility = View.GONE
            type_text.visibility = View.GONE
            export_button.isEnabled = false
        }
    }

    override fun loadData(onError: (Exception) -> Unit) {
        lifecycleScope.launch {
            val(exception, account, spaceUsage) = withContext(Dispatchers.IO) {
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
                Triple(exception, account, spaceUsage)
            }
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

    private fun onUploadComplete(cancelled: Boolean) {
        if (cancelled) {
            context?.let {
                val builder = AlertDialog.Builder(it)
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_export))
                    setMessage("Dropbox export was cancelled")
                    setPositiveButton("OK") { dialog, which -> }
                    show()
                }
            }
        } else {
            context?.let {
                val builder = AlertDialog.Builder(it)
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_export))
                    setMessage("Dropbox export completed")
                    setPositiveButton("OK") { dialog, which -> }
                    show()
                }
            }
        }
    }

    private fun onUploadError(ex: Exception) {
        context?.let {
            val builder = AlertDialog.Builder(it)
            with(builder) {
                setTitle(resources.getString(R.string.dropbox_export))
                setMessage("Dropbox export failed: " + ex.message)
                setPositiveButton("OK") { dialog, which -> }
                show()
            }
        }
    }

    companion object {
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
