package com.mthaler.knittings

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
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
import com.mthaler.knittings.databinding.ActivityNeedleListBinding
import com.mthaler.knittings.filter.*
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.settings.SettingsActivity
import com.mthaler.knittings.utils.AndroidViewModelFactory
import com.mthaler.knittings.whatsnew.WhatsNewDialog
import java.util.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
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
}