package com.mthaler.knittings.compressphotos

import androidx.fragment.app.Fragment
import com.mthaler.knittings.databinding.FragmentCompressPhotoBinding

class CompressPhotoFragment : Fragment() {

    private var _binding: FragmentCompressPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}