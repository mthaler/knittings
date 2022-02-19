package com.mthaler.knittings.compressphotos

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentCompressPhotoBinding
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.Format

class CompressPhotoFragment : Fragment() {

    private var _binding: FragmentCompressPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(CompressPhotosViewModel::class.java)
        viewModel.statistics.observe(this, { statistics ->
            binding.numberOfPhotos.text = statistics.photos.toString()
            binding.totalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
            binding.willBeCompressed.text = statistics.photosToCompress.toString()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CompressPhotosServiceManager.getInstance().jobStatus.observe(viewLifecycleOwner, { jobStatus->
            when(jobStatus) {
                is JobStatus.Initialized -> {
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Progress -> {
                    binding.buttonStart.isEnabled = false
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.VISIBLE
                    binding.cancelButton.visibility = View.VISIBLE
                    binding.progressBar.progress = jobStatus.value
                    binding.result.visibility = View.GONE
                }
                is JobStatus.Success -> {
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
                is JobStatus.Cancelled -> {
                    CompressPhotosServiceManager.getInstance().cancelled = false
                    binding.buttonStart.isEnabled = true
                    binding.compressingPhotosTitle.visibility = View.VISIBLE
                    binding.progressBar.visibility = View.GONE
                    binding.cancelButton.visibility = View.GONE
                    binding.result.visibility = View.VISIBLE
                    binding.result.text = jobStatus.msg
                }
            }
        })

        CompressPhotosServiceManager.getInstance().serviceStatus.observe(viewLifecycleOwner, { serviceStatus ->
            when (serviceStatus) {
                ServiceStatus.Stopped -> binding.buttonStart.isEnabled = true
                ServiceStatus.Started -> binding.buttonStart.isEnabled = false
            }
        })

        binding.buttonStart.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle(resources.getString(R.string.compress_photos_dialog_title))
                setMessage(resources.getString(R.string.compress_photos_dialog_message))
                setPositiveButton(resources.getString(R.string.compress_photos_dialog_button_compress)) { dialog, which ->
                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                    val request = OneTimeWorkRequestBuilder<CompressPhotoWorker>().setConstraints(constraints).build()
                    val workManager = WorkManager.getInstance(requireContext())
                    workManager.enqueueUniqueWork(TAG,  ExistingWorkPolicy.REPLACE, request)
                    val workInfo = workManager.getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner) { workInfo ->
                        when (workInfo?.state) {
                            WorkInfo.State.RUNNING -> CompressPhotosServiceManager.getInstance().updateServiceStatus(com.mthaler.knittings.service.ServiceStatus.Started)
                            else -> CompressPhotosServiceManager.getInstance().updateServiceStatus(com.mthaler.knittings.service.ServiceStatus.Stopped)
                        }
                    }
                }
                setNegativeButton(resources.getString(R.string.dialog_button_cancel)) { dialog, which -> }
                show()
            }
        }

        binding.cancelButton.setOnClickListener {
            CompressPhotosServiceManager.getInstance().cancelled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "com.mthaler.knittings.compressphotos.CompressPhotoFragment"

        fun newInstance() = CompressPhotoFragment()

    }
}