package com.mthaler.knittings.dropbox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NavUtils
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.ListFolderResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.runCatching
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentDropboxImportBinding
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DropboxImportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxImportBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val wakeLock: PowerManager.WakeLock =
                (requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                        acquire()
                    }
                }
            try {
                lifecycleScope.launchWhenStarted {
                    import(lifecycleScope)
                }
            } finally {
                wakeLock.release()
            }
        } else {
            Toast.makeText(requireContext(), "Access network state permission denied", Toast.LENGTH_SHORT).show()
        }
    }


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
        binding.loginButton.setOnClickListener { startDropboxAuthorization() }

        binding.importButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_NETWORK_STATE)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                else -> throw IllegalArgumentException("Unknown jobStatus: " + jobStatus)
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
            when (serviceStatus) {
                ServiceStatus.Stopped -> binding.importButton.isEnabled = true
                ServiceStatus.Started -> binding.importButton.isEnabled = false
                else -> throw IllegalArgumentException("Unknown serviceStatus: " + serviceStatus)
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
                fetchAccountInfo()
            }
            credential
        } else localCredential


        if (credential == null) {
            with(binding) {
                // user is logged out, show login button, hide other buttons
                loginButton.visibility = View.VISIBLE
                account.visibility = View.GONE
                emailText.visibility = View.GONE
                nameText.visibility = View.GONE
                typeText.visibility = View.GONE
                importButton.isEnabled = false
            }
        } else {
            with(binding) {
                //logoutButton.visibility = View.VISIBLE
                loginButton.visibility = android.view.View.GONE
                account.visibility = android.view.View.VISIBLE
                emailText.visibility = android.view.View.VISIBLE
                nameText.visibility = android.view.View.VISIBLE
                typeText.visibility = android.view.View.VISIBLE
                importButton.isEnabled = true
                fetchAccountInfo()
            }
        }
    }

    private suspend fun import(lifecycleScope: LifecycleCoroutineScope) {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient, requireContext(), viewLifecycleOwner)
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_import))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { _, _ ->
                        try {
                            lifecycleScope.launchWhenStarted {
                                val result = dropboxApi.listFolders()
                                importDatabase(result)
                            }
                        } catch (ex: java.lang.Exception) {
                            throw ex
                        }
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { _, _ -> }
                    show()
                }
            } else {
                val result = runCatching { lifecycleScope.launch(Dispatchers.IO){ dropboxApi.listFolders() } }
                when (result) {
                    is Ok ->
                        try {
                            lifecycleScope.launchWhenStarted() {
                                val r = dropboxApi.listFolders()
                                importDatabase(r)
                            }
                        } catch (ex: java.lang.Exception) {
                            throw ex
                        }
                    is Err ->
                        throw result.error
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
            val dropboxApi = DropboxApi(dropboxClient, requireContext(), viewLifecycleOwner)
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Backups")
            dialogBuilder.setItems(files) { _, item ->
                val directory = files[item]
                lifecycleScope.launch(Dispatchers.IO) {
                    dropboxApi.readDatabase(directory)
                }
            }
            dialogBuilder.setNegativeButton("Cancel") { _, _ -> }
            // Create alert dialog object via builder
            val alertDialogObject = dialogBuilder.create()
            // Show the dialog
            alertDialogObject.show()
        }
    }

    private fun fetchAccountInfo() {
        val clientIdentifier = "DropboxSampleAndroid/1.0.0"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient, requireContext(), viewLifecycleOwner)
            lifecycleScope.launch {
                when (val response = dropboxApi.getAccountInfo()) {
                    is DropboxAccountInfoResponse.Failure -> {
                        Toast.makeText(
                            requireContext(),
                            "Error getting account info!",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.eaccount.text =
                            "type: ${response.exception.javaClass} + ${response.exception.localizedMessage}"
                    }
                    is DropboxAccountInfoResponse.Success -> {
                        binding.account.text = response.accountInfo.toString()
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

    override fun clearData() {
        binding.loginButton.visibility = View.VISIBLE
    }
}
