package com.mthaler.knittings

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import com.mthaler.knittings.databinding.ActivityMainBinding
import com.mthaler.knittings.details.EditKnittingDetailsFragment
import com.mthaler.knittings.needle.EditNeedleFragment

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val f = MainFragment()
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            ft.add(R.id.knitting_list_container, f)
            ft.commit()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add toolbar to activity
        setSupportActionBar(binding.toolbar)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val f = supportFragmentManager.findFragmentById(R.id.knitting_list_container)
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
            val f = supportFragmentManager.findFragmentById(R.id.knitting_details_container)
            if (f is EditKnittingDetailsFragment) {
                f.onBackPressed()
            }
        } else {
            val f = supportFragmentManager.findFragmentById(R.id.knitting_details_container)
            if (f is EditKnittingDetailsFragment) {
                f.onBackPressed()
            } else {
                super.onBackPressed()
            }
        }
    }
}