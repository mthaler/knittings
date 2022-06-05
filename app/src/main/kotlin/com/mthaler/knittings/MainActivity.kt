package com.mthaler.knittings

import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import com.mthaler.knittings.about.AboutDialog
import com.mthaler.knittings.compressphotos.CompressPhotosActivity
import com.mthaler.knittings.database.KnittingsDataSource
import com.mthaler.knittings.projectcount.ProjectCountActivity
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.filter.*
import com.mthaler.knittings.model.Status
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.settings.SettingsActivity
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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