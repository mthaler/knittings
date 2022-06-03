package com.mthaler.knittings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.details.KnittingDetailsActivity

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    private var initialQuery: CharSequence? = null
    private var sv: SearchView? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMainBinding.inflate(inflater, container, false)
        val view = binding.root
        setHasOptionsMenu(true)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            initialQuery = savedInstanceState.getCharSequence(MainActivity.STATE_QUERY)
        }

        // set on click handler of floating action button that creates a new knitting
        binding.fabCreateAddKnitting.setOnClickListener {
            // start knitting activity with newly created knitting
            startActivity(KnittingDetailsActivity.newIntent(this, -1L, true))
        }
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
     }
}