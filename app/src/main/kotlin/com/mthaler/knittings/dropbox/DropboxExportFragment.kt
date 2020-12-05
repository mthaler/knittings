package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.users.FullAccount
import com.dropbox.core.v2.users.SpaceUsage
import com.mthaler.dbapp.dropbox.DropboxClientFactory
import com.mthaler.dbapp.dropbox.DropboxExportViewModel
import com.mthaler.knittings.R
import com.mthaler.dbapp.service.JobStatus
import com.mthaler.dbapp.service.ServiceStatus
import com.mthaler.dbapp.utils.Format
import com.mthaler.knittings.utils.NetworkUtils
import com.mthaler.dbapp.utils.StringUtils.formatBytes
import com.mthaler.knittings.databinding.FragmentDropboxExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DropboxExportFragment : AbstractDropboxFragment() {

    private var _binding: FragmentDropboxExportBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retain this fragment across configuration changes.
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDropboxExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener { Auth.startOAuth2Authentication(context, AppKey) }

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
                                    viewLifecycleOwner.lifecycleScope.launch {
                                        withContext(Dispatchers.IO) {
                                            DropboxClientFactory.getClient().files().deleteV2("/${jobStatus.data}")
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

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(DropboxExportViewModel::class.java)
        viewModel.statistics.observe(viewLifecycleOwner, { statistics ->
            binding.photoCount.text = statistics.photos.toString()
            binding.photoTotalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
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
            binding.dataTitle.visibility = View.VISIBLE
            binding.exportButton.isEnabled = true
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.account.visibility = View.GONE
            binding.emailText.visibility = View.GONE
            binding.nameText.visibility = View.GONE
            binding.typeText.visibility = View.GONE
            binding.dataTitle.visibility = View.GONE
            binding.exportButton.isEnabled = false
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
                    binding.emailText.text = account.email
                    binding.nameText.text = account.name.displayName
                    binding.typeText.text = account.accountType.name
                }
                if (spaceUsage != null) {
                    binding.maxSpaceText.text = "Max: " + formatBytes(spaceUsage.allocation.individualValue.allocated)
                    binding.usedSpaceText.text = "Used: " + formatBytes(spaceUsage.used)
                    binding.freeSpaceText.text = "Free: " + formatBytes(spaceUsage.allocation.individualValue.allocated - spaceUsage.used)
                }
            }
        }
    }

    companion object {
        private const val AppKey = "6ybf7tgqdbhf641"
    }
}
