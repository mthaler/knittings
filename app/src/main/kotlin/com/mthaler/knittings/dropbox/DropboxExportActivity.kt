package com.mthaler.knittings.dropbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityDropboxExportBinding
import com.mthaler.knittings.service.JobStatus

class DropboxExportActivity : AbstractDropboxActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDropboxExportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val f = supportFragmentManager.findFragmentByTag(DropboxExportFragmentTag)

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (f == null) {
            val ff = DropboxExportFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, ff, DropboxExportFragmentTag).commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.dropbox_export, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            R.id.menu_item_dropbox_logout -> {
//                logout()
//                true
//            }
            android.R.id.home -> {
                // Respond to the action bar's Up/Home button
                val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
                if (upIntent == null) {
                    throw IllegalStateException("No Parent Activity Intent")
                } else {
                    val sm = DropboxExportServiceManager.getInstance()
                    if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
                        sm.updateJobStatus(JobStatus.Initialized)
                    }
                    NavUtils.navigateUpTo(this, upIntent)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val sm = DropboxExportServiceManager.getInstance()
        if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
            sm.updateJobStatus(JobStatus.Initialized)
        }
        super.onBackPressed()
    }

    override fun updateFragment() {
        val ft = supportFragmentManager.beginTransaction()
        val f = DropboxExportFragment()
        ft.replace(R.id.fragment_container, f)
        ft.addToBackStack(null)
        ft.commit()
    }

    companion object {
        const val DropboxExportFragmentTag = "dropbox_export_fragment"

        fun newIntent(context: Context): Intent = Intent(context, DropboxExportActivity::class.java)
    }
}