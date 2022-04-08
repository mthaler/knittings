package com.mthaler.knittings.compressphotos

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentCompressPhotoBinding
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.service.ServiceStatus
import com.mthaler.knittings.utils.Format
import com.mthaler.knittings.utils.WorkerUtils
import java.lang.Exception

class CompressPhotosFragment : Fragment() {

    private var _binding: FragmentCompressPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCompressPhotoBinding.inflate(inflater, container, false)
        val view = binding.root

        val viewModel = ViewModelProvider(requireActivity()).get(CompressPhotosViewModel::class.java)
        viewModel.statistics.observe(viewLifecycleOwner, { statistics ->
            binding.numberOfPhotos.text = statistics.photos.toString()
            binding.totalSize.text = Format.humanReadableByteCountBin(statistics.totalSize)
            binding.willBeCompressed.text = statistics.photosToCompress.toString()
        })

        binding.buttonStart.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            with(builder) {
                setTitle(resources.getString(R.string.compress_photos_dialog_title))
                setMessage(resources.getString(R.string.compress_photos_dialog_message))
                setPositiveButton(resources.getString(R.string.compress_photos_dialog_button_compress)) { dialog, which ->
                    val request = OneTimeWorkRequestBuilder<CompressPhotoWorker>().build()
                    val workManager = WorkManager.getInstance(requireContext())
                    workManager.enqueueUniqueWork(TAG,  ExistingWorkPolicy.REPLACE, request)
                    workManager.getWorkInfoByIdLiveData(request.id).observe(viewLifecycleOwner) { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.ENQUEUED -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Progress(0))
                                    sm.updateServiceStatus(ServiceStatus.Started)
                                }
                                WorkInfo.State.RUNNING -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Progress(workInfo.progress.getInt(WorkerUtils.Progress, 0)))
                                    sm.updateServiceStatus(ServiceStatus.Started)
                                }
                                WorkInfo.State.SUCCEEDED -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Success(context.resources.getString(R.string.compress_photos_completed)))
                                    sm.updateServiceStatus(ServiceStatus.Stopped)
                                }
                                WorkInfo.State.FAILED -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Error(Exception("Could not compress photos")))
                                    sm.updateServiceStatus(ServiceStatus.Stopped)
                                }
                                WorkInfo.State.BLOCKED -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Error(Exception("Could not compress photos")))
                                    sm.updateServiceStatus(ServiceStatus.Stopped)
                                }
                                WorkInfo.State.CANCELLED -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Cancelled(context.resources.getString(R.string.compress_photos_cancelled)))
                                    sm.updateServiceStatus(ServiceStatus.Started)
                                }
                                else -> {
                                    val sm = CompressPhotosServiceManager.getInstance()
                                    sm.updateJobStatus(JobStatus.Progress(0))
                                    sm.updateServiceStatus(ServiceStatus.Stopped)
                                }
                            }
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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val TAG = "com.mthaler.knittings.compressphotos.CompressPhotoFragment"

        @JvmStatic
        fun newInstance() = CompressPhotosFragment()

    }
}