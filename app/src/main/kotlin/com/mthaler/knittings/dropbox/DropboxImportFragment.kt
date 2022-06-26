package com.mthaler.knittings.dropbox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentDropboxImportBinding
import com.mthaler.knittings.model.toDatabase
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream

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
                    import()
                }
            } finally {
                wakeLock.release()
            }
        } else {
            Toast.makeText(requireContext(), "Access network state permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override val APP_KEY = BuildConfig.DROPBOX_KEY

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
             when {
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED -> {
                    val wakeLock: PowerManager.WakeLock =
                    (requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                        newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                            acquire()
                        }
                    }
                    try {
                        lifecycleScope.launchWhenStarted {
                            import()
                        }
                    } finally {
                        wakeLock.release()
                    }
                }
                ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.WAKE_LOCK) -> {
                    Toast.makeText(requireContext(), "Wake_Lock permission required", Toast.LENGTH_SHORT)
                    requestPermissionLauncher.launch(Manifest.permission.WAKE_LOCK)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.WAKE_LOCK)
                    }
                }
            }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        binding.account.text =
                            "type: ${response.exception.javaClass} + ${response.exception.localizedMessage}"
                    }
                    is DropboxAccountInfoResponse.Success -> {
                        binding.emailText.text = response.accountInfo.email
                        binding.nameText.text = response.accountInfo.name.displayName
                        binding.typeText.text = response.accountInfo.accountType.name
                    }
                }
            }
        }
    }

    private fun readDatabase(directory: String, credential: DbxCredential) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val clientIdentifier = "DropboxSampleAndroid/1.0.0"
            val requestConfig = DbxRequestConfig(clientIdentifier)
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val deferred = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                val os = ByteArrayOutputStream()
                dropboxClient.files().download("/$directory/db.json").download(os)
                val bytes = os.toByteArray()
                val jsonStr = String(bytes)
                val json = JSONObject(jsonStr)
                val externalFilesDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
                val database = json.toDatabase(requireContext(), externalFilesDir)
                database.checkValidity()
                val entries = dropboxClient.files().listFolder("/$directory").entries
                val ids = entries.filter { it.name != "db.json" }.map { FileUtils.getFilenameWithoutExtension(it.name).toLong() }.toHashSet()
                Pair(database, ids)
            }
            val (database, idsFromPhotoFiles) = deferred.await()
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


    override fun clearData() {
        binding.loginButton.visibility = View.VISIBLE
    }
}
