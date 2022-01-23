package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.users.Account
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.mthaler.knittings.DatabaseApplication
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentDropboxExportBinding
import com.mthaler.knittings.model.Project
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.Format
import com.mthaler.knittings.utils.NetworkUtils
import com.mthaler.knittings.utils.StringUtils.formatBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

class DropboxExportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxExportBinding? = null
    private val binding get() = _binding!!
    private val adapter = FilesAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.exportButton.setOnClickListener {
            val isWiFi = NetworkUtils.isWifiConnected(requireContext())
            if (!isWiFi) {
                val builder = AlertDialog.Builder(requireContext())
                with(builder) {
                    setTitle(resources.getString(R.string.dropbox_export))
                    setMessage(resources.getString(R.string.dropbox_export_no_wifi_question))
                    setPositiveButton(resources.getString(R.string.dropbox_export_dialog_export_button)) { dialog, which ->
                        DropboxExportService.startService(requireContext())
                        DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
                    }
                    setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                    show()
                }
            } else {
                DropboxExportService.startService(requireContext())
                DropboxExportServiceManager.getInstance().updateJobStatus(JobStatus.Progress(0))
            }
        }

        binding.cancelButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle(resources.getString(R.string.dropbox_export_cancel_dialog_title))
                setMessage(resources.getString(R.string.dropbox_export_cancel_dialog_message))
                setPositiveButton(resources.getString(R.string.dropbox_export_cancel_dialog_ok_button)) { dialog, which ->
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
                                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                        DropboxClientFactory.getClient().files().deleteV2("/${jobStatus.data}")
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
            sm.serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
                when (serviceStatus) {
                    ServiceStatus.Stopped -> binding.exportButton.isEnabled = true
                    ServiceStatus.Started -> binding.exportButton.isEnabled = false
                }
            })

            val viewModel = ViewModelProvider(
                this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
            ).get(DropboxExportViewModel::class.java)
            viewModel.statistics.observe(viewLifecycleOwner, { statistics ->
                binding.photoCount.text = statistics.photos.toString()
                binding.photoTotalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
            })

            //Check if we have an existing token stored, this will be used by DbxClient to make requests
            val localCredential: DbxCredential? = getLocalCredential()
            val credential: DbxCredential? = if (localCredential == null) {
                val credential = Auth.getDbxCredential() //fetch the result from the AuthActivity
                credential?.let {
                    //the user successfully connected their Dropbox account!
                    storeCredentialLocally(it)
                    fetchAccountInfo()
                    fetchDropboxFolder()
                }
                credential
            } else localCredential

            if (credential == null) {
                with(binding) {
                    loginButton.visibility = View.VISIBLE
                    logoutButton.visibility = View.GONE
                    uploadButton.isEnabled = false
                }
            } else {
                with(binding) {
                    uploadButton.isEnabled = true
                    logoutButton.visibility = View.VISIBLE
                    loginButton.visibility = View.GONE
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (hasToken()) {
            // user is logged in, hide login button, show other buttons
            binding.dataTitle.visibility = View.VISIBLE
            binding.exportButton.isEnabled = true
        } else {
            // user is logged out, show login button, hide other buttons
            binding.dataTitle.visibility = View.GONE
            binding.exportButton.isEnabled = false
        }
    }

    // called from onResume after the DropboxClientFactory is initialized
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
            /*if (exception != null) {
                onError(exception)
            } else {
                if (account != null) {
                    binding.emailText.text = account.email
                    binding.nameText.text = account.name.displayName
                    binding.typeText.text = account.accountType.name
                }
                if (spaceUsage != null) {
                    binding.maxSpaceText.text = "Max: " + formatBytes(spaceUsage.allocation.individualValue.allocated)
                    binding.usedSpaceText.text = "Used: " + formatBytes(spaceUsage.used)
                    binding.freeSpaceText.text = "Free: " + formatBytes(spaceUsage.allocation.individualValue.allocated - spaceUsage.used)
                }
            }*/
        }
    }

    private fun fetchAccountInfo() {
        val clientIdentifier = "DropboxSampleAndroid/1.0.0"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient)
            lifecycleScope.launch {
                when (val response = dropboxApi.getAccountInfo()) {
                    is DropboxAccountInfoResponse.Failure -> {
                        Toast.makeText(
                            this@DropboxExportFragment.requireContext(),
                            "Error getting account info!",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.exceptionText.text =
                            "type: ${response.exception.javaClass} + ${response.exception.localizedMessage}"
                    }
                    is DropboxAccountInfoResponse.Success -> {
                        val profileImageUrl = response.accountInfo.profilePhotoUrl
                        Glide.with(this@DropboxExportFragment.requireActivity()).load(profileImageUrl)
                            .into(binding.accountPhoto)
                    }
                }
            }
        }
    }

    private fun fetchDropboxFolder() {
        val clientIdentifier = "DropboxSampleAndroid/1.0.0"
        val requestConfig = DbxRequestConfig(clientIdentifier)
        val credential = getLocalCredential()
        credential?.let {
            val dropboxClient = DbxClientV2(requestConfig, credential)
            val dropboxApi = DropboxApi(dropboxClient)

            lifecycleScope.launch {
                dropboxApi.getFilesForFolderFlow("").collect {
                    when (it) {
                        is GetFilesResponse.Failure -> {
                            Toast.makeText(
                                this@DropboxExportFragment.requireContext(),
                                "Error getting Dropbox files!",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.exceptionText.text =
                                "type: ${it.exception.javaClass} + ${it.exception.localizedMessage}"
                        }
                        is GetFilesResponse.Success -> {
                            adapter.submitList(it.result)
                        }
                    }
                }
            }
        }
    }
}
