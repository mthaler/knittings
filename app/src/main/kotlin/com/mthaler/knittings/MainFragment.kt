package com.mthaler.knittings

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.mthaler.knittings.about.AboutDialog
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.compressphotos.CompressPhotosActivity
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.databinding.FragmentMainBinding
import com.mthaler.knittings.details.KnittingDetailsFragment
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import com.mthaler.knittings.filter.CombinedFilter
import com.mthaler.knittings.filter.ContainsFilter
import com.mthaler.knittings.filter.SingleCategoryFilter
import com.mthaler.knittings.filter.SingleStatusFilter
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.projectcount.ProjectCountActivity
import com.mthaler.knittings.settings.SettingsActivity
import com.mthaler.knittings.utils.AndroidViewModelFactory
import com.mthaler.knittings.whatsnew.WhatsNewDialog
import java.util.*

class MainFragment : Fragment(), SearchView.OnQueryTextListener, SearchView.OnCloseListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var viewModel: MainViewModel

    private var initialQuery: CharSequence? = null
    private var sv: SearchView? = null

    private lateinit var toggle: ActionBarDrawerToggle

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY)
        }

        setHasOptionsMenu(true)

        init()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        val drawer = binding.drawerLayout
        toggle = ActionBarDrawerToggle(requireActivity(), drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        // Where do I put this?
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = binding.knittingRecyclerView
        rv.layoutManager = LinearLayoutManager(requireContext())

        val adapter = KnittingAdapter(requireContext(), { knitting ->
            requireActivity().supportFragmentManager.commit {
                replace(R.id.knitting_list_container, KnittingDetailsFragment.newInstance(knitting.id))
                setReorderingAllowed(true)
                addToBackStack(null) // name can be null
            }
        }, { knitting ->
            (requireActivity() as AppCompatActivity).startSupportActionMode(object : ActionMode.Callback {
                /**
                 * Called to report a user click on an action button.
                 *
                 * @param mode The current ActionMode
                 * @param menu The item that was clicked
                 * @return true if this callback handled the event, false if the standard MenuItem invocation should continue.
                 */
                override fun onActionItemClicked(mode: ActionMode?, menu: MenuItem?): Boolean {
                    when (menu?.itemId) {
                        R.id.action_delete -> {
                            mode?.finish()
                            DeleteDialog.create(requireContext(), knitting.title) {
                                KnittingsDataSource.deleteProject(knitting)
                            }.show()
                            return true
                        }
                        R.id.action_copy -> {
                            val newTitle = "${knitting.title} - ${getString(R.string.copy)}"
                            val knittingCopy = knitting.copy(
                                title = newTitle,
                                started = Date(),
                                finished = null,
                                defaultPhoto = null,
                                rating = 0.0,
                                duration = 0,
                                status = Status.PLANNED
                            )
                            KnittingsDataSource.addProject(knittingCopy)
                            mode?.finish()
                            return true
                        }
                        else -> {
                            return false
                        }
                    }
                }

                /**
                 * Called when action mode is first created. The menu supplied will be used to generate action buttons for the action mode.
                 *
                 * @param mode The current ActionMode
                 * @param menu Menu used to populate action buttons
                 * @return true if the action mode should be created, false if entering this mode should be aborted.
                 */
                override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                    val inflater = mode?.menuInflater
                    inflater?.inflate(R.menu.knitting_list_action, menu)
                    return true
                }

                /**
                 * Called to refresh an action mode's action menu whenever it is invalidated.
                 *
                 * @param mode The current ActionMode
                 * @param menu Menu used to populate action buttons
                 */
                override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = true

                /**
                 * Called when an action mode is about to be exited and destroyed.
                 *
                 * @param mode The current ActionMode
                 */
                override fun onDestroyActionMode(mode: ActionMode?) {
                }
            })
        })

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

        binding.knittingRecyclerView.adapter = adapter

        requireActivity().onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.knitting_list, menu)
        configureSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return when (item.itemId) {
            R.id.menu_item_about -> {
                AboutDialog.show(requireActivity())
                true
            }
            R.id.menu_item_sort -> {
                val listItems = arrayOf(
                    getString(R.string.sorting_newest_first),
                    getString(R.string.sorting_oldest_first),
                    getString(R.string.sorting_alphabetical)
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
            R.id.menu_item_clear_filters -> {
                viewModel.filter = CombinedFilter.empty()
                true
            }
            R.id.menu_item_category_filter -> {
                val categories = KnittingsDataSource.allCategories.sortedBy { it.name.lowercase() }
                val listItems =
                    (listOf(getString(R.string.filter_show_all)) + categories.map { it.name }
                        .toList()).toTypedArray()
                val builder = AlertDialog.Builder(requireContext())
                val f = viewModel.filter
                val checkedItem: Int = let {
                    val result = f.filters.find { it is SingleCategoryFilter }
                    if (result != null && result is SingleCategoryFilter) {
                        val index = categories.indexOf(result.category)
                        index + 1
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which ->
                    when (which) {
                        0 -> viewModel.filter =
                            CombinedFilter(f.filters.filterNot { it is SingleCategoryFilter })
                        else -> {
                            val category = categories[which - 1]
                            val newFilter = SingleCategoryFilter(category)
                            viewModel.filter =
                                CombinedFilter(f.filters.filterNot { it is SingleCategoryFilter } + newFilter)
                        }
                    }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_status_filter -> {
                val listItems =
                    (listOf(getString(R.string.filter_show_all)) + Status.formattedValues(requireContext())).toTypedArray()
                val builder = AlertDialog.Builder(requireContext())
                val f = viewModel.filter
                val checkedItem = let {
                    val result = f.filters.find { it is SingleStatusFilter }
                    if (result != null && result is SingleStatusFilter) {
                        val index = Status.values().indexOf(result.status)
                        index + 1
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which ->
                    when (which) {
                        0 -> viewModel.filter =
                            CombinedFilter(f.filters.filterNot { it is SingleStatusFilter })
                        else -> {
                            val status = Status.values()[which - 1]
                            val newFilter = SingleStatusFilter(status)
                            viewModel.filter =
                                CombinedFilter(f.filters.filterNot { it is SingleStatusFilter } + newFilter)
                        }
                    }
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, _ -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_count -> {
                startActivity(ProjectCountActivity.newIntent(requireContext()))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun init() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        var currentVersionNumber = 0
        val savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0)
        try {
            val pi = requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)
            if (Build.VERSION.SDK_INT >= 27) {
                currentVersionNumber = pi.longVersionCode.toInt()
            } else {
                currentVersionNumber = pi.versionCode
            }
        } catch (e: Exception) {
        }
        if (currentVersionNumber > savedVersionNumber) {
            WhatsNewDialog.show(requireActivity())
            val editor = sharedPref.edit()
            editor.putInt(VERSION_KEY, currentVersionNumber)
            editor.commit()
        }
    }

    private fun createFilterText(hasCategoryFilter: Boolean, hasStatusFilter: Boolean): String {
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_edit_categories -> {
                startActivity(CategoryListActivity.newIntent(requireContext()))
            }
            R.id.nav_edit_needles -> {
                startActivity(NeedleListActivity.newIntent(requireContext()))
            }
            R.id.nav_compress_photos -> {
                startActivity(CompressPhotosActivity.newIntent(requireContext()))
            }
            R.id.nav_dropbox_export -> {
                startActivity(DropboxExportActivity.newIntent(requireContext()))
            }
            R.id.nav_dropbox_import -> {
                startActivity(DropboxImportActivity.newIntent(requireContext()))
            }
            R.id.nav_edit_settings -> {
                startActivity(SettingsActivity.newIntent(requireContext()))
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

     companion object {
         private const val STATE_QUERY = "q"
         private const val VERSION_KEY = "version_number"
     }
}