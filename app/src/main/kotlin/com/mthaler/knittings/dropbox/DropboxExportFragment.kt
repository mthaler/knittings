package com.mthaler.knittings.dropbox

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.google.android.material.snackbar.Snackbar
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentDropboxExportBinding
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.Format
import com.mthaler.knittings.utils.isWifiConnected
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

class DropboxExportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxExportBinding? = null
    private val binding get() = _binding!!

    override protected val APP_KEY = BuildConfig.DROPBOX_KEY

    private lateinit var workManager: WorkManager

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val wakeLock: PowerManager.WakeLock =
                (requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                    newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Knittings::DropboxImport").apply {
                        acquire()
                    }
                }
            try {
                export()
            } finally {
                wakeLock.release()
            }
        } else {
            Toast.makeText(requireContext(), "Access network state permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDropboxExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // this opens a web browser where the user can log in
        binding.loginButton.setOnClickListener { Auth.startOAuth2Authentication(context, (requireContext().applicationContext as DatabaseApplication).dropboxAppKey) }

        binding.exportButton.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WAKE_LOCK
                ) == PackageManager.PERMISSION_GRANTED -> {
                    val wakeLock: PowerManager.WakeLock =
                        (requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                            newWakeLock(
                                PowerManager.PARTIAL_WAKE_LOCK,
                                "Knittings::DropboxImport"
                            ).apply {
                                acquire()
                            }
                        }
                    try {
                        val credential = getLocalCredential()
                        credential?.let {
                            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                                export()
                            }
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

            workManager = WorkManager.getInstance(requireContext())
        }

        binding.cancelButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle(resources.getString(R.string.dropbox_export_cancel_dialog_title))
                setMessage(resources.getString(R.string.dropbox_export_cancel_dialog_message))
                setPositiveButton(resources.getString(R.string.dropbox_export_cancel_dialog_ok_button)) { _, _ ->
                    DropboxExportServiceManager.getInstance().cancelled = true
                }
                show()
            }
        }

        val sm = DropboxExportServiceManager.getInstance()

        sm.jobStatus.observe(viewLifecycleOwner, { jobStatus ->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    binding.exportButton.isEnabled = true
                    binding.exportTitle.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    binding.exportButton.isEnabled = false
                    binding.exportTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.VISIBLE
                    binding.result.visibility = View.GONE
                    binding.progressBar.progress = jobStatus.value
                }
                is JobStatus.Cancelled -> {
                    DropboxExportServiceManager.getInstance().cancelled = false
                    binding.exportButton.isEnabled = true
                    binding.exportTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
                is JobStatus.Success -> {
                    binding.exportButton.isEnabled = true
                    binding.exportTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
                else -> throw IllegalArgumentException("Unknown jobStatus: " + jobStatus)
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
            when(serviceStatus) {
                ServiceStatus.Stopped -> binding.exportButton.isEnabled = true
                ServiceStatus.Started -> binding.exportButton.isEnabled = false
                else -> throw IllegalArgumentException("Unknown serviceStatus: " + serviceStatus)
            }
        })

        val viewModel = ViewModelProvider(requireActivity()).get(DropboxExportViewModel::class.java)
        viewModel.statistics.observe(viewLifecycleOwner, { statistics ->
            binding.photoCount.text = statistics.photos.toString()
            binding.photoTotalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                        val sb = Snackbar.make(binding.loginButton, "Please log out of dropbox and log in again!", Snackbar.LENGTH_LONG)
                        sb.setAction(R.string.dropbox_export, object : View.OnClickListener {
                            override fun onClick(v: View?) {
                                throw response.exception
                            }
                        })
                        sb.show()
                    }
                    is DropboxAccountInfoResponse.Success -> {
                        val account= response.accountInfo
                        binding.emailText.text = account.email
                        binding.nameText.text = account.name.displayName
                        binding.typeText.text = account.accountType.name
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                    val sm = DropboxExportServiceManager.getInstance()
                    if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
                        sm.updateJobStatus(JobStatus.Initialized)
                    }
                    NavUtils.navigateUpTo(requireActivity(), upIntent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun export() {
        val isWiFi = requireContext().isWifiConnected()
        if (!isWiFi) {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle(resources.getString(R.string.dropbox_export))
                setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { _, _ ->
                    val request = OneTimeWorkRequestBuilder<DropboxExportWorker>().build()
                    workManager.enqueueUniqueWork(DropboxExportWorker.TAG,  ExistingWorkPolicy.REPLACE, request)
                    DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                }
                setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { _, _ ->
                    val sm = DropboxExportServiceManager.getInstance()
                    sm.cancelled = true
                    sm.updateJobStatus(JobStatus.Cancelled())
                }
                show()
            }
        } else {
            val request = OneTimeWorkRequestBuilder<DropboxExportWorker>().build()
            val workManager = WorkManager.getInstance(requireContext())
            workManager.enqueueUniqueWork(TAG,  ExistingWorkPolicy.REPLACE, request)
            DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
        }
    }

    override fun clearData() {
        binding.loginButton.visibility = View.VISIBLE
    }
}
