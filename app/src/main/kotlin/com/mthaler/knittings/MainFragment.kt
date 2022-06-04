package com.mthaler.knittings

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mthaler.knittings.about.AboutDialog
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.databinding.FragmentMainBinding
import com.mthaler.knittings.details.KnittingDetailsActivity
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.filter.ContainsFilter
import com.mthaler.knittings.filter.SingleCategoryFilter
import com.mthaler.knittings.filter.SingleStatusFilter
import com.mthaler.knittings.utils.AndroidViewModelFactory
import com.mthaler.knittings.whatsnew.WhatsNewDialog

class MainFragment : Fragment(), SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private lateinit var viewModel: MainViewModel

    private var initialQuery: CharSequence? = null
    private var sv: SearchView? = null

    private lateinit var adapter: KnittingAdapter

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

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

        val rv = binding.knittingRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())

        init()
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        setHasOptionsMenu(true)
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activeFilters = binding.knittingActiveFilters

        viewModel = AndroidViewModelFactory(requireActivity().application).create(MainViewModel::class.java)
        viewModel.projects.observe(viewLifecycleOwner, { knittings ->

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_about -> {
                AboutDialog.show(requireActivity())
                true
            }
            R.id.menu_item_sort -> {
                val listItems = arrayOf(
                    resources.getString(R.string.sorting_newest_first),
                    resources.getString(R.string.sorting_oldest_first),
                    resources.getString(R.string.sorting_alphabetical)
                )
                val builder = AlertDialog.Builder(requireContext())
                val checkedItem = when (viewModel.sorting) {
                    Sorting.NewestFirst -> 0
                    Sorting.OldestFirst -> 1
                    Sorting.Alphabetical -> 2
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which ->
                    when (which) {
                        0 -> viewModel.sorting = Sorting.NewestFirst
                        1 -> viewModel.sorting = Sorting.OldestFirst
                        2 -> viewModel.sorting = Sorting.Alphabetical
                    }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
     * Called when the query text is changed by the use
     *
     * @param newText the new content of the query text field
     * @return false if the SearchView should perform the default action of showing any suggestions
     *         if available, true if the action was handled by the listener
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText == null || TextUtils.isEmpty(newText)) {
            viewModel.filter = CombinedFilter.empty()
        } else {
            viewModel.filter = CombinedFilter(listOf(ContainsFilter(newText)))
        }
        return true
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