package com.mthaler.knittings.dropbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.compressphotos.CompressPhotoWorker
import com.mthaler.knittings.compressphotos.CompressPhotosFragment
import com.mthaler.knittings.compressphotos.CompressPhotosServiceManager
import com.mthaler.knittings.databinding.FragmentDropboxImportBinding
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.FileUtils
import com.mthaler.knittings.utils.NetworkUtils
import com.mthaler.knittings.utils.Try
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Exception

class DropboxImportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxImportBinding? = null
    private val binding get() = _binding!!

    override val APP_KEY = BuildConfig.DROPBOX_KEY
    override fun exception(ex: String) {
        binding.exceptionText.text = ex
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDropboxImportBinding.inflate(inflater, container, false)

        // this opens a web browser where the user can log in
        binding.loginButton.setOnClickListener {
            startDropboxAuthorization()
        }

        binding.importButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                import()
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sm = DropboxImportServiceManager.getInstance()

        sm.jobStatus.observe(viewLifecycleOwner, { jobStatus ->
            when (jobStatus) {
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
            when (serviceStatus) {
                ServiceStatus.Stopped -> binding.importButton.isEnabled = true
                ServiceStatus.Started -> binding.importButton.isEnabled = false
            }
        })
    }


    override fun onResume() {
        super.onResume()

        //Check if we have an existing token stored, this will be used by DbxClient to make requests
        val localCredential: DbxCredential? = getLocalCredential()
        val credential: DbxCredential? = if (localCredential == null) {
            val credential = Auth.getDbxCredential() //fetch the result from the AuthActivity
            credential?.let {
                //the user successfully connected their Dropbox account!
                storeCredentialLocally(it)
            }
            credential
        } else localCredential

        if (credential == null) {
            with(binding) {
                loginButton.visibility = View.VISIBLE
                //logoutButton.visibility = View.GONE
            }
        } else {
            with(binding) {
                //logoutButton.visibility = View.VISIBLE
                loginButton.visibility = View.GONE
            }
        }

        if (credential != null) {
            // user is logged in, hide login button, show other buttons
            binding.loginButton.visibility = View.GONE
            binding.account.visibility = View.VISIBLE
            binding.emailText.visibility = View.VISIBLE
            binding.nameText.visibility = View.VISIBLE
            binding.typeText.visibility = View.VISIBLE
            binding.importButton.isEnabled = true
            fetchAccountInfo()
        } else {
            // user is logged out, show login button, hide other buttons
            binding.loginButton.visibility = View.VISIBLE
            binding.account.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.nameText.visibility = View.GONE
            binding.typeText.visibility = View.GONE
            binding.importButton.isEnabled = false
        }
    }

    private suspend fun import() {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient)
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_import))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { dialog, which ->
                        try {
                            lifecycleScope.launchWhenStarted() {
                                val result = dropboxApi.listFolders()
                                importDatabase(result)
                            }
                        } catch (ex: java.lang.Exception) {
                            throw ex
                        }
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            } else {
                val result = Try { GlobalScope.launch(Dispatchers.IO) { dropboxApi.listFolders() } }
                when (result) {
                    is Try.Success ->
                        try {
                            lifecycleScope.launchWhenStarted() {
                                val result = dropboxApi.listFolders()
                                importDatabase(result)
                            }
                        } catch (ex: java.lang.Exception) {
                            throw ex
                        }
                    is Try.Failure ->
                        throw result.exception
                }
            }
        }
    }

    private suspend fun importDatabase(result: ListFolderResult) {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        credential?.let {
            val files = result.entries.map { it.name }.sortedDescending().toTypedArray()
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Backups")
            dialogBuilder.setItems(files) { dialog, item ->
                val directory = files[item]
                lifecycleScope.launch(Dispatchers.IO) {

                    dropboxApi.readDatabase(directory, requireContext())
                }
            }
            dialogBuilder.setNegativeButton("Cancel") { dialog, which -> }
            // Create alert dialog object via builder
            val alertDialogObject = dialogBuilder.create()
            // Show the dialog
            alertDialogObject.show()
        }
    }

    private fun fetchAccountInfo() {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient)
            lifecycleScope.launch {
                when (val response = dropboxApi.getAccountInfo()) {
                    is DropboxAccountInfoResponse.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Error getting account info!",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.exceptionText.text =
                            "type: ${response.exception.javaClass} + ${response.exception.localizedMessage}"
                    }
                    is DropboxAccountInfoResponse.Success -> {
                        val account = response.accountInfo
                        binding.emailText.text = account.email
                        binding.nameText.text = account.name.displayName
                        binding.typeText.text = account.accountType.name
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_item_dropbox_logout -> {
            logout()
            true
        }
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(requireActivity())
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val sm = DropboxImportServiceManager.getInstance()
                if (sm.jobStatus.value is JobStatus.Success) {
                    sm.updateJobStatus(JobStatus.Initialized)
                }
                NavUtils.navigateUpTo(requireActivity(), upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    /*suspend fun readDatabase(directory: String, ctx: Context) {
        val (database, idsFromPhotoFiles) = withContext(Dispatchers.IO) {
            val os = ByteArrayOutputStream()
            dropboxClient.files().download("/$directory/db.json").download(os)
            val bytes = os.toByteArray()
            val jsonStr = String(bytes)
            val json = JSONObject(jsonStr)
            val externalFilesDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
            val database = (ctx.applicationContext as DatabaseApplication<Project>).createExportDatabaseFromJSON(json, externalFilesDir
            )
            database.checkValidity()
            val entries = dropboxClient.files().listFolder("/$directory").entries
            val ids = entries.filter { it.name != "db.json" }
                .map { FileUtils.getFilenameWithoutExtension(it.name).toLong() }.toHashSet()
            Pair(database, ids)
        }
        val ids = database.photos.map { it.id }.toHashSet()
        val missingPhotos = ids - idsFromPhotoFiles
        if (missingPhotos.isNotEmpty()) {
            withContext(Dispatchers.Main) {
                val builder = AlertDialog.Builder(ctx)
                with(builder) {
                    setTitle(R.string.dropbox_import_dialog_title)
                    setMessage(
                        ctx.resources.getString(
                            R.string.dropbox_import_dialog_incomplete_msg,
                            missingPhotos.size as Any
                        )
                    )
                    setPositiveButton(R.string.dropbox_import_dialog_button_import) { dialog, which ->
                        val filteredDatabase = database.removeMissingPhotos(missingPhotos)
                        DropboxImportService.startService(ctx, directory, filteredDatabase)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(ctx.resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                val builder = AlertDialog.Builder(ctx)
                with(builder) {
                    setTitle(ctx.resources.getString(R.string.dropbox_import_dialog_title))
                    setMessage(ctx.resources.getString(R.string.dropbox_import_dialog_msg))
                    setPositiveButton(ctx.resources.getString(R.string.dropbox_import_dialog_button_import)) { dialog, which ->
                        DropboxImportService.startService(ctx, directory, database)
                        DropboxImportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(ctx.resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            }
        }
    }*/

    protected override fun clearData() {
        binding.loginButton.visibility = View.VISIBLE
    }
}
