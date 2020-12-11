package com.mthaler.knittings.dropbox

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.files.ListFolderResult
import com.mthaler.knittings.R
import com.mthaler.dbapp.utils.NetworkUtils
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.mthaler.dbapp.dropbox.AbstractDropboxFragment
import com.mthaler.dbapp.dropbox.DropboxClientFactory
import com.mthaler.dbapp.dropbox.DropboxImportServiceManager
import com.mthaler.dbapp.utils.FileUtils
import com.mthaler.knittings.model.toDatabase
import com.mthaler.dbapp.service.JobStatus
import com.mthaler.dbapp.service.ServiceStatus
import com.mthaler.knittings.databinding.FragmentDropboxImportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class DropboxImportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxImportBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDropboxImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener { Auth.startOAuth2Authentication(context, AppKey) }

        binding.importButton.setOnClickListener {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sm = DropboxImportServiceManager.getInstance()

        sm.jobStatus.observe(viewLifecycleOwner, { jobStatus ->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    binding.importButton.isEnabled = true
                    binding.importTitle.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    binding.importButton.isEnabled = false
                    binding.importTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.result.visibility = View.GONE
                    binding.progressBar.progress = jobStatus.value
                }
                is JobStatus.Success -> {
                    binding.importButton.isEnabled = true
                    binding.importTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
            when(serviceStatus) {
                ServiceStatus.Stopped -> binding.importButton.isEnabled = true
                ServiceStatus.Started -> binding.importButton.isEnabled = false
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            binding.loginButton.visibility = View.GONE
            binding.account.visibility = View.VISIBLE
            binding.emailText.visibility = View.VISIBLE
            binding.nameText.visibility = View.VISIBLE
            binding.typeText.visibility = View.VISIBLE
            binding.importButton.isEnabled = true
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.account.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.nameText.visibility = View.GONE
            binding.typeText.visibility = View.GONE
            binding.importButton.isEnabled = false
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
            binding.emailText.text = account.email
            binding.nameText.text = account.name.displayName
            binding.typeText.text = account.accountType.name
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
                val externalFilesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                val database = json.toDatabase(requireContext(), externalFilesDir)
                database.checkValidity()
                val entries = dbxClient.files().listFolder("/$directory").entries
                val ids = entries.filter { it.name != "db.json" }.map { FileUtils.getFilenameWithoutExtension(it.name).toLong() }.toHashSet()
                Pair(database, ids)
            }
            val ids = database.photos.map { it.id}.toHashSet()
            val missingPhotos = ids - idsFromPhotoFiles
            if (missingPhotos.isNotEmpty()) {
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
