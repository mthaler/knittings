package com.mthaler.knittings

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import com.mthaler.knittings.about.AboutDialog
import com.mthaler.knittings.compressphotos.CompressPhotosActivity
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.details.KnittingDetailsActivity
import com.mthaler.knittings.projectcount.ProjectCountActivity
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.filter.*
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.settings.SettingsActivity
import com.mthaler.knittings.whatsnew.WhatsNewDialog
import java.lang.ClassCastException
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private lateinit var viewModel: MainViewModel

    private var initialQuery: CharSequence? = null
    private var sv: SearchView? = null

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add toolbar to activity
        setSupportActionBar(binding.toolbar)

        // set on click handler of floating action button that creates a new knitting
        binding.fabCreateAddKnitting.setOnClickListener {
            // start knitting activity with newly created knitting
            startActivity(KnittingDetailsActivity.newIntent(this, -1L, true))
        }

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val rv = findViewById<RecyclerView>(R.id.knitting_recycler_view)
        rv.layoutManager = LinearLayoutManager(this)

        val adapter = KnittingAdapter(this, { knitting ->
            startActivity(KnittingDetailsActivity.newIntent(this, knitting.id, false))
        }, { knitting ->
            startSupportActionMode(object : ActionMode.Callback {
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
                            DeleteDialog.create(this@MainActivity, knitting.title) {
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
        rv.adapter = adapter

        val activeFilters = binding.knittingActiveFilters

        viewModel =  ViewModelProvider(this).get(MainViewModel::class.java)
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

        init()
    }

    private fun init() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        var currentVersionNumber = 0L
        val savedVersionNumber: Int = try {
            sharedPref.getInt(VERSION_KEY, 0)
        } catch (ex: ClassCastException) {
            sharedPref.getLong(VERSION_KEY, 0L).toInt()
        }
        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= 27) {
                currentVersionNumber = pi.longVersionCode
            } else {
                currentVersionNumber = pi.versionCode.toLong()
            }
        } catch (e: Exception) {
        }
        if (currentVersionNumber > savedVersionNumber) {
            WhatsNewDialog.show(this)
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

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting_list, menu)
        configureSearchView(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_about -> {
                AboutDialog.show(this)
                true
            }
            R.id.menu_item_sort -> {
                val listItems = arrayOf(
                    getString(R.string.sorting_newest_first),
                    getString(R.string.sorting_oldest_first),
                    getString(R.string.sorting_alphabetical)
                )
                val builder = AlertDialog.Builder(this)
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
                val builder = AlertDialog.Builder(this)
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
                    (listOf(getString(R.string.filter_show_all)) + Status.formattedValues(this)).toTypedArray()
                val builder = AlertDialog.Builder(this)
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
                startActivity(ProjectCountActivity.newIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_edit_categories -> {
                startActivity(CategoryListActivity.newIntent(this))
            }
            R.id.nav_edit_needles -> {
                startActivity(NeedleListActivity.newIntent(this))
            }
            R.id.nav_compress_photos -> {
                startActivity(CompressPhotosActivity.newIntent(this))
            }
            R.id.nav_dropbox_export -> {
                startActivity(DropboxExportActivity.newIntent(this))
            }
            R.id.nav_dropbox_import -> {
                startActivity(DropboxImportActivity.newIntent(this))
            }
            R.id.nav_edit_settings -> {
                startActivity(SettingsActivity.newIntent(this))
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
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
        private val STATE_QUERY = "q"
        private const val VERSION_KEY = "version_number"
    }
}