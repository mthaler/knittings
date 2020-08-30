package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.files.ListFolderResult
import com.mthaler.knittings.R
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.android.synthetic.main.fragment_dropbox_import.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DropboxImportFragment : AbstractDropboxFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_dropbox_import, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        login_button.setOnClickListener { Auth.startOAuth2Authentication(context, AppKey) }

        import_button.setOnClickListener {
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_import))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { dialog, which ->
                        lifecycleScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                DropboxClientFactory.getClient().files().listFolder("")
                            }
                            onListFolder(result)
                        }
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            } else {
                lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        DropboxClientFactory.getClient().files().listFolder("")
                    }
                    onListFolder(result)
                }
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sm = DropboxImportServiceManager.getInstance()

        sm.jobStatus.observe(viewLifecycleOwner, Observer { jobStatus ->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    import_button.isEnabled = true
                    import_title.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    import_button.isEnabled = false
                    import_title.visibility = View.VISIBLE
                    progressBar.visibility = View.VISIBLE
                    result.visibility = View.GONE
                    progressBar.progress = jobStatus.value
                }
                is JobStatus.Success -> {
                    import_button.isEnabled = true
                    import_title.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    result.visibility = View.VISIBLE
                    result.text = jobStatus.msg
                }
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, Observer { serviceStatus ->
            when(serviceStatus) {
                ServiceStatus.Stopped -> import_button.isEnabled = true
                ServiceStatus.Started -> import_button.isEnabled = false
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
            import_button.isEnabled = true
        } else {
            login_button.visibility = View.VISIBLE
            account.visibility = View.GONE
            email_text.visibility = View.GONE
            name_text.visibility = View.GONE
            type_text.visibility = View.GONE
            import_button.isEnabled = false
        }
    }

    override fun loadData(onError: (Exception) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            val account = withContext(Dispatchers.IO) {
                val client = DropboxClientFactory.getClient()
                val account = client.users().currentAccount
                //val spaceUsage = client.users().spaceUsage
                account
            }
            email_text.text = account.email
            name_text.text = account.name.displayName
            type_text.text = account.accountType.name
        }
    }

    private fun onListFolder(result: ListFolderResult?) {
        if (result != null) {
            val files = result.entries.map { it.name }.sortedDescending().toTypedArray()
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Backups")
            dialogBuilder.setItems(files) { dialog, item ->
                val directory = files[item]
                readDatabase(directory)
            }
            dialogBuilder.setNegativeButton("Cancel") { dialog, which -> }
            // Create alert dialog object via builder
            val alertDialogObject = dialogBuilder.create()
            // Show the dialog
            alertDialogObject.show()
        } else {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle("List folders")
                setMessage("Error when listing folders: null")
                setPositiveButton("OK") { dialog, which -> }
                show()
            }
        }
    }

    private fun readDatabase(directory: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val (database, idsFromPhotoFiles) = withContext(Dispatchers.IO) {
                val dbxClient = DropboxClientFactory.getClient()
                val os = ByteArrayOutputStream()
                dbxClient.files().download("/$directory/db.json").download(os)
                val bytes = os.toByteArray()
                val jsonStr = String(bytes)
                val json = JSONObject(jsonStr)
                val externalFilesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                val database = json.toDatabase(requireContext(), externalFilesDir)
                database.checkValidity()
                val entries = dbxClient.files().listFolder("/$directory").entries
                val ids = entries.filter { it.name != "db.json" }.map { FileUtils.getFilenameWithoutExtension(it.name).toLong() }.toHashSet()
                Pair(database, ids)
            }
            val ids = database.photos.map { it.id}.toHashSet()
            val missingPhotos = ids - idsFromPhotoFiles
            if (missingPhotos.size > 0) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(R.string.dropbox_import_dialog_title)
                    setMessage(getString(R.string.dropbox_import_dialog_incomplete_msg, missingPhotos.size as Any))
                    setPositiveButton(R.string.dropbox_import_dialog_button_import) { dialog, which ->
                        val filteredPhotos = database.photos.filterNot { missingPhotos.contains(it.id) }
                        val updatedKnittings = database.knittings.map { if (missingPhotos.contains(it.defaultPhoto?.id)) it.copy(defaultPhoto = null) else it }
                        val filteredDatabase = database.copy(knittings = updatedKnittings, photos = filteredPhotos)
                        filteredDatabase.checkValidity()
                        DropboxImportService.startService(requireContext(), directory, filteredDatabase)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which ->}
                    show()
                }
            } else {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(getString(R.string.dropbox_import_dialog_title))
                    setMessage(getString(R.string.dropbox_import_dialog_msg))
                    setPositiveButton(getString(R.string.dropbox_import_dialog_button_import)) { dialog, which ->
                        DropboxImportService.startService(requireContext(), directory, database)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            }
        }
    }

    companion object {
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
