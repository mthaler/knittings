package com.mthaler.knittings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mthaler.knittings.databinding.FragmentBaseBinding

class BaseFragment: Fragment() {

    private var _binding: FragmentBaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configureToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureToolbar() {

    }
}