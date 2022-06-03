package com.mthaler.knittings

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.details.KnittingDetailsActivity
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.filter.SingleCategoryFilter
import com.mthaler.knittings.filter.SingleStatusFilter
import com.mthaler.knittings.utils.AndroidViewModelFactory
import com.mthaler.knittings.whatsnew.WhatsNewDialog

class MainFragment : Fragment(), NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

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

        viewModel = AndroidViewModelFactory(requireActivity().application).create(MainViewModel::class.java)
        viewModel.projects.observe(this, { knittings ->

            when {
                knittings == null -> {
                    binding.knittingListEmptyRecyclerView.visibility = View.GONE
                    binding.knittingRecyclerView.visibility = View.VISIBLE
                }
                knittings.isEmpty() -> {
                    binding.knittingListEmptyRecyclerView.visibility = View.VISIBLE
                    binding.knittingRecyclerView.visibility = View.GONE
                }
                else -> {
                    binding.knittingListEmptyRecyclerView.visibility = View.GONE
                    binding.knittingRecyclerView.visibility = View.VISIBLE
                }
            }
            if (viewModel.filter.filters.filter { it is SingleCategoryFilter || it is SingleStatusFilter }
                    .isEmpty()) {
                activeFilters.text = ""
                activeFilters.visibility = View.GONE
            } else {
                val hasCategoryFilter =
                    viewModel.filter.filters.filter { it is SingleCategoryFilter }.isNotEmpty()
                val hasStatusFilter =
                    viewModel.filter.filters.filter { it is SingleStatusFilter }.isNotEmpty()
                val filterText = createFilterText(hasCategoryFilter, hasStatusFilter)
                activeFilters.text = filterText
                activeFilters.visibility = View.VISIBLE
            }
            adapter.setKnittings(knittings ?: emptyList())
        })
    }

    private fun init() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var currentVersionNumber = 0L
        val savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0)
        try {
            val pi = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            if (Build.VERSION.SDK_INT >= 27) {
                currentVersionNumber = pi.longVersionCode
            } else {
                currentVersionNumber = pi.versionCode.toLong()
            }
        } catch (e: Exception) {
        }
        if (currentVersionNumber > savedVersionNumber) {
            WhatsNewDialog.show(requireActivity())
            val editor = sharedPref.edit()
            editor.putLong(VERSION_KEY, currentVersionNumber)
            editor.commit()
        }
    }

    fun createFilterText(hasCategoryFilter: Boolean, hasStatusFilter: Boolean): String {
        val sb = StringBuilder()
        if (hasCategoryFilter) {
            sb.append(resources.getString(R.string.category))
        }
        if (hasCategoryFilter && hasStatusFilter) {
            sb.append(", ")
        }
        if (hasStatusFilter) {
            sb.append(resources.getString(R.string.status))
        }
        return sb.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val sv = this.sv
        if (sv != null && !sv.isIconified) {
            outState.putCharSequence(STATE_QUERY, sv.query)
        }

        super.onSaveInstanceState(outState)
    }

    private fun configureSearchView(menu: Menu) {
        val search = menu.findItem(R.id.search)
        val sv = search.actionView as SearchView
        sv.setOnQueryTextListener(this)
        sv.setOnCloseListener(this)
        sv.isSubmitButtonEnabled = false
        sv.setIconifiedByDefault(true)
        if (initialQuery != null) {
            sv.isIconified = false
            search.expandActionView()
            sv.setQuery(initialQuery, true)
        }
        this.sv = sv
    }

    /**
     * Called when the user submits the query. This could be due to a key press on the keyboard or due to pressing a submit button.
     * The listener can override the standard behavior by returning true to indicate that it has handled the submit request.
     * Otherwise return false to let the SearchView handle the submission by launching any associated intent.
     *
     * @param newText new content of the query text field
     * @return true if the query has been handled by the listener, false to let the SearchView perform the default action.
     */
    override fun onQueryTextSubmit(newText: String?): Boolean = false

    /**
     * The user is attempting to close the SearchView.
     *
     * @return true if the listener wants to override the default behavior of clearing the text field and dismissing it, false otherwise.
     */
    override fun onClose(): Boolean {
        viewModel.filter = CombinedFilter.empty()
        return true
    }

     companion object {
         private const val STATE_QUERY = "q"
         private const val VERSION_KEY = "version_number"
     }
}