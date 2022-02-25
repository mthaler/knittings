package com.mthaler.knittings.dropbox

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.databinding.ActivityDropboxExportBinding
import com.mthaler.knittings.service.JobStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DropboxExportActivity : BaseActivity() {

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

    override fun onBackPressed() {
        val sm = DropboxExportServiceManager.getInstance()
        if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
            sm.updateJobStatus(JobStatus.Initialized)
        }
        super.onBackPressed()
    }


    companion object {
        const val DropboxExportFragmentTag = "dropbox_export_fragment"

        fun newIntent(context: Context): Intent = Intent(context, DropboxExportActivity::class.java)
    }
}