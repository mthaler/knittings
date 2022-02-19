package com.mthaler.knittings.compressphotos

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.work.*
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.FragmentCompressPhotoBinding

class CompressPhotoFragment : Fragment() {

    private var _binding: FragmentCompressPhotoBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    if(workInfo?.state == WorkInfo.State.SUCCEEDED) {
                        }
                    }
                    //CompressPhotosService.startService(requireContext(), "Foreground Service is running...")
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
    }
}