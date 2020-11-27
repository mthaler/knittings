package com.mthaler.knittings.dropbox

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.mthaler.dbapp.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.dbapp.service.JobStatus
import com.mthaler.knittings.databinding.ActivityDropboxImportBinding

class DropboxImportActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityDropboxImportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val f = supportFragmentManager.findFragmentByTag(DropboxImportFragmentTag)

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (f == null) {
            val ff = DropboxImportFragment()
            supportFragmentManager.beginTransaction().add(R.id.frament_container, ff, DropboxImportFragmentTag).commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val sm = DropboxImportServiceManager.getInstance()
                if (sm.jobStatus.value is JobStatus.Success) {
                    sm.updateJobStatus(JobStatus.Initialized)
                }
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val sm = DropboxImportServiceManager.getInstance()
        if (sm.jobStatus.value is JobStatus.Success) {
            sm.updateJobStatus(JobStatus.Initialized)
        }
        super.onBackPressed()
    }

    companion object {
        const val DropboxImportFragmentTag = "dropbox_import_fragment"

        fun newIntent(context: Context): Intent = Intent(context, DropboxImportActivity::class.java)
    }
}
