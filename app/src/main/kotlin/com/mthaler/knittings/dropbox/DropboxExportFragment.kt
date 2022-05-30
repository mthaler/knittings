package com.mthaler.knittings.dropbox

import android.Manifest
import android.app.AlertDialog
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
import androidx.core.app.NavUtils
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.mthaler.knittings.BuildConfig
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentDropboxExportBinding
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.AndroidViewModelFactory
import com.mthaler.knittings.utils.Format
import com.mthaler.knittings.utils.NetworkUtils
import com.mthaler.knittings.utils.WorkerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DropboxExportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxExportBinding? = null
    private val binding get() = _binding!!

    override protected val APP_KEY = BuildConfig.DROPBOX_KEY

    private val requestMultiplePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())  { permissions ->
        if (permissions != null && permissions.size == 1) {
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
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun exception(ex: String) {
        binding.exceptionText.text = ex
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
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_export))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { _, _ ->
                        DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                        export()
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { _, _ -> }
                    show()
                }
            } else {
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                export()
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

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
                    when (jobStatus.data) {
                        is String -> {
                            val builder = AlertDialog.Builder(requireContext())
                            with(builder) {
                                setTitle(R.string.dropbox_export_cancelled_dialog_title)
                                setMessage(R.string.dropbox_export_cancelled_dialog_msg)
                                setPositiveButton(R.string.dropbox_export_cancelled_dialog_ok_button) { dialog, which ->
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        withContext(Dispatchers.IO) {
                                            //DropboxClientFactory.getClient().files().deleteV2("/${jobStatus.data}")
                                        }
                                    }
                                }
                                show()
                            }
                        }
                    }
                }
                is JobStatus.Success -> {
                    binding.exportButton.isEnabled = true
                    binding.exportTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
            }
        })

        sm.serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
            when(serviceStatus) {
                ServiceStatus.Stopped -> binding.exportButton.isEnabled = true
                ServiceStatus.Started -> binding.exportButton.isEnabled = false
            }
        })

        val viewModel = AndroidViewModelFactory(requireActivity().application).create(DropboxExportViewModel::class.java)
        viewModel.statistics.observe(viewLifecycleOwner, { statistics ->
            binding.photoCount.text = statistics.photos.toString()
            binding.photoTotalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
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
            }
        } else {
            with(binding) {
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
            binding.dataTitle.visibility = View.VISIBLE
            binding.exportButton.isEnabled = true
            fetchAccountInfo()
        } else {
            // user is logged out, show login button, hide other buttons
            binding.loginButton.visibility = View.VISIBLE
            binding.account.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.nameText.visibility = View.GONE
            binding.typeText.visibility = View.GONE
            binding.dataTitle.visibility = View.GONE
            binding.exportButton.isEnabled = false
        }
    }


    private fun fetchAccountInfo() {
        val requestConfig = DbxRequestConfig(CLIENT_IDENTIFIER)
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
                        binding.exceptionText.text =
                            "type: ${response.exception.javaClass} + ${response.exception.localizedMessage}"
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
        requestMultiplePermissions.launch(arrayOf(Manifest.permission.ACCESS_WIFI_STATE))
        val request = OneTimeWorkRequestBuilder<DropboxExportWorker>().build()
        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueueUniqueWork(DropboxExportWorker.TAG,  ExistingWorkPolicy.REPLACE, request)
        workManager.getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner) { workInfo ->
            if (workInfo != null) {
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(JobStatus.Progress(0))
                        sm.updateServiceStatus(ServiceStatus.Started)
                    }
                    WorkInfo.State.RUNNING -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(
                            JobStatus.Progress(
                                workInfo.progress.getInt(
                                    WorkerUtils.Progress,
                                    0
                                )
                            )
                        )
                        sm.updateServiceStatus(ServiceStatus.Started)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(JobStatus.Success(requireContext().resources.getString(R.string.compress_photos_completed)))
                        sm.updateServiceStatus(ServiceStatus.Stopped)
                    }
                    WorkInfo.State.FAILED -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(JobStatus.Error(Exception("Could not compress photos")))
                        sm.updateServiceStatus(ServiceStatus.Stopped)
                    }
                    WorkInfo.State.BLOCKED -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(JobStatus.Error(Exception("Could not compress photos")))
                        sm.updateServiceStatus(ServiceStatus.Stopped)
                    }
                    WorkInfo.State.CANCELLED -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(
                            JobStatus.Cancelled(
                                requireContext().resources.getString(
                                    R.string.compress_photos_cancelled
                                )
                            )
                        )
                        sm.updateServiceStatus(ServiceStatus.Started)
                    }
                    else -> {
                        val sm = DropboxExportServiceManager.getInstance()
                        sm.updateJobStatus(JobStatus.Progress(0))
                        sm.updateServiceStatus(ServiceStatus.Stopped)
                    }
                }
            }
        }
    }

    protected override fun clearData() {
        binding.loginButton.visibility = View.VISIBLE
    }
}
