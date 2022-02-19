package com.mthaler.knittings.compressphotos

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.NetworkType
import com.mthaler.knittings.databinding.FragmentCompressPhotoBinding

class CompressPhotoFragment : Fragment() {

    private var _binding: FragmentCompressPhotoBinding? = null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}