package com.mthaler.knittings

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.mthaler.knittings.about.AboutDialog
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import org.jetbrains.anko.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.activity_main.*
import com.mthaler.knittings.database.datasource
import com.mthaler.knittings.details.KnittingDetailsActivity
import com.mthaler.knittings.Extras.EXTRA_KNITTING_ID
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.settings.SettingsActivity
import kotlinx.android.synthetic.main.content_main.*
import java.lang.StringBuilder

/**
 * The main activity that gets displayed when the app is started. It displays a list of knitting projects.
 * The user can add new projects or edit existing ones
 */
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private var sorting: Sorting = Sorting.NewestFirst
    private var filter: CombinedFilter = CombinedFilter.Empty
    private var initialQuery: CharSequence? = null
    private var sv: SearchView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // add toolbar to activity
        setSupportActionBar(toolbar)

        // set on click handler of floating action button that creates a new knitting
        fab_create_add_knitting.setOnClickListener {
            // start knitting activity with newly created knitting
            val knitting = datasource.createKnitting()
            startActivity<KnittingDetailsActivity>(EXTRA_KNITTING_ID to knitting.id, KnittingDetailsActivity.EXTRA_EDIT_ONLY to true)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)

        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        val rv = findViewById<RecyclerView>(R.id.knitting_recycler_view)
        rv.layoutManager = LinearLayoutManager(this)

        if (savedInstanceState != null) {
            sorting = Sorting.valueOf(savedInstanceState.getString("sorting"))
            filter = savedInstanceState.getSerializable("filter") as CombinedFilter
            initialQuery = savedInstanceState.getCharSequence(STATE_QUERY)
        }
    }

    /**
     * The onResume method is called when the activity is started or if the user returns from another activity
     * e.g. the KnittingDetailsActivity.
     */
    override fun onResume() {
        super.onResume()
        updateKnittingList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("sorting", sorting.name)
        outState.putSerializable("filter", filter)
        val sv = this.sv
        if (sv != null && !sv.isIconified) {
            outState.putCharSequence(STATE_QUERY, sv.query)
        }

        super.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * This is only called once, the first time the options menu is displayed.
     *
     * @param menu The options menu in which you place your items.
     * @return you must return true for the menu to be displayed; if you return false it will not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.knitting_list, menu)
        configureSearchView(menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * @param item the menu item that was selected.
     * @return return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_about -> {
                AboutDialog.show(this)
                true
            }
            R.id.menu_item_sort -> {
                val listItems = arrayOf(getString(R.string.sorting_newest_first), getString(R.string.sorting_oldest_first), getString(R.string.sorting_alphabetical))
                val builder = AlertDialog.Builder(this)
                val checkedItem = when(sorting) {
                    Sorting.NewestFirst -> 0
                    Sorting.OldestFirst -> 1
                    Sorting.Alphabetical -> 2
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                    0 -> sorting = Sorting.NewestFirst
                    1 -> sorting = Sorting.OldestFirst
                    2 -> sorting = Sorting.Alphabetical
                  }
                    updateKnittingList()
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_clear_filters -> {
                filter = CombinedFilter.Empty
                updateKnittingList()
                true
            }
            R.id.menu_item_category_filter -> {
                val categories = datasource.allCategories
                categories.sortedBy { it.name }
                val listItems = (listOf(getString(R.string.filter_show_all)) + categories.map { it.name }.toList()).toTypedArray()
                val builder = AlertDialog.Builder(this)
                val f = filter
                val checkedItem: Int = let {
                    val result = f.filters.find { it is SingleCategoryFilter }
                    if (result != null && result is SingleCategoryFilter) {
                        val index = categories.indexOf(result.category)
                        index + 1
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                    0 -> filter = CombinedFilter(f.filters.filterNot { it is SingleCategoryFilter })
                    else -> {
                        val category = categories[which - 1]
                        val newFilter = SingleCategoryFilter(category)
                        filter = CombinedFilter(f.filters.filterNot { it is SingleCategoryFilter } + newFilter)
                    }
                }
                    updateKnittingList()
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
            }
            R.id.menu_item_status_filter -> {
                val listItems = (listOf(getString(R.string.filter_show_all)) + Status.formattedValues(this)).toTypedArray()
                val builder = AlertDialog.Builder(this)
                val f = filter
                val checkedItem = let {
                    val result = f.filters.find { it is SingleStatusFilter }
                    if (result != null && result is SingleStatusFilter) {
                        val index = Status.values().indexOf(result.status)
                        index + 1
                    } else {
                        0
                    }
                }
                builder.setSingleChoiceItems(listItems, checkedItem) { dialog, which -> when(which) {
                    0 -> filter = CombinedFilter(f.filters.filterNot { it is SingleStatusFilter })
                    else -> {
                        val status = Status.values()[which - 1]
                        val newFilter = SingleStatusFilter(status)
                        filter = CombinedFilter(f.filters.filterNot { it is SingleStatusFilter } + newFilter)
                    }
                }
                    updateKnittingList()
                    dialog.dismiss()
                }
                builder.setNegativeButton(R.string.dialog_button_cancel) { dialog, which -> dialog.dismiss() }
                val dialog = builder.create()
                dialog.show()
                true
                true
            }
            R.id.menu_item_count -> {
                val builder = AlertDialog.Builder(this)
                startActivity<ProjectCountActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item the selected item
     * @return true to display the item as the selected item
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_dropbox_export -> {
                startActivity<DropboxExportActivity>()
            }
            R.id.nav_dropbox_import -> {
                startActivity<DropboxImportActivity>()
            }
            R.id.nav_edit_categories -> {
                startActivity<CategoryListActivity>()
            }
            R.id.nav_edit_needles -> {
                startActivity<NeedleListActivity>()
            }
            R.id.nav_edit_settings -> {
                startActivity<SettingsActivity>()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Updates the list of categories
     */
    private fun updateKnittingList() {
        val activeFilters = findViewById<TextView>(R.id.knitting_active_filters)
        val rv = findViewById<RecyclerView>(R.id.knitting_recycler_view)
        val knittings = datasource.allKnittings
        if (knittings.isEmpty()) {
            knitting_list_empty_recycler_view.visibility = View.VISIBLE
            knitting_recycler_view.visibility = View.GONE
        } else {
            knitting_list_empty_recycler_view.visibility = View.GONE
            knitting_recycler_view.visibility = View.VISIBLE
        }
        if (filter.filters.filter { it is SingleCategoryFilter || it is SingleStatusFilter }.isEmpty()) {
            activeFilters.text = ""
            activeFilters.visibility = View.GONE
        } else {
            val sb = StringBuilder()
            sb.append(resources.getString(R.string.active_filters))
            sb.append(": ")
            val hasCategoryFilter = filter.filters.filter { it is SingleCategoryFilter }.isNotEmpty()
            val hasStatusFilter = filter.filters.filter { it is SingleStatusFilter }.isNotEmpty()
            if (hasCategoryFilter) {
                sb.append(resources.getString(R.string.category))
            }
            if (hasCategoryFilter && hasStatusFilter) {
                sb.append(", ")
            }
            if (hasStatusFilter) {
                sb.append(resources.getString(R.string.status))
            }
            activeFilters.text = sb.toString()
            activeFilters.visibility = View.VISIBLE
        }
        when(sorting) {
            Sorting.NewestFirst -> knittings.sortByDescending { it.started}
            Sorting.OldestFirst -> knittings.sortBy { it.started }
            Sorting.Alphabetical -> knittings.sortBy { it.title.toLowerCase() }
        }
        val filtered = filter.filter(knittings)
        // start EditCategoryActivity if the users clicks on a category
        val adapter = KnittingAdapter(this, filtered, {
            knitting -> startActivity<KnittingDetailsActivity>(EXTRA_KNITTING_ID to knitting.id, KnittingDetailsActivity.EXTRA_EDIT_ONLY to false)
        }, { knitting ->})
        rv.adapter = adapter
    }

    private fun configureSearchView(menu: Menu) {
        val search = menu.findItem(R.id.search)
        val sv = search.actionView as SearchView
        sv.setOnQueryTextListener(this)
        sv.setOnCloseListener(this)
        sv.isSubmitButtonEnabled = false
        sv.setIconifiedByDefault(true)
        if (initialQuery != null)
        {
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
            filter = CombinedFilter.Empty
        } else {
            filter = CombinedFilter(listOf(ContainsFilter(newText)))
        }
        updateKnittingList()
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
        filter = CombinedFilter.Empty
        return true
    }

    companion object {
        private val STATE_QUERY = "q"
    }
}
