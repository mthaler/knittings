package com.mthaler.knittings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.mthaler.knittings.category.CategoryListActivity
import com.mthaler.knittings.dropbox.DropboxExportActivity
import com.mthaler.knittings.dropbox.DropboxImportActivity
import com.mthaler.knittings.compressphotos.CompressPhotosActivity
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.needle.EditNeedleFragment
import com.mthaler.knittings.needle.NeedleListActivity
import com.mthaler.knittings.settings.SettingsActivity

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            val f = MainFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.knitting_list_container, f)
            ft.commit()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val f = supportFragmentManager.findFragmentById(R.id.needle_list_container)
                if (f is EditNeedleFragment) {
                    f.onBackPressed()
                } else {
                    NavUtils.navigateUpTo(this, upIntent)
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
}