package com.mthaler.knittings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY)
        }

        // set on click handler of floating action button that creates a new knitting
        binding.fabCreateAddKnitting.setOnClickListener {
            // start knitting activity with newly created knitting
            startActivity(KnittingDetailsActivity.newIntent(requireContext(), -1L, true))
        }

        val toggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val rv = binding.knittingRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())
    }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)
     }

     companion object {
         private val STATE_QUERY = "q"
     }
}