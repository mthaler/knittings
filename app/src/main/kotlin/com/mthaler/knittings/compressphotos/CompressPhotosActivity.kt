package com.mthaler.knittings.compressphotos

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.core.app.NavUtils
import com.mthaler.knittings.BaseActivity
import com.mthaler.knittings.R
import com.mthaler.knittings.service.JobStatus
import com.mthaler.knittings.databinding.ActivityCompressPhotosBinding

class CompressPhotosActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCompressPhotosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // enable up navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val f = CompressPhotosFragment.newInstance()
        val ft = supportFragmentManager.beginTransaction()
        ft.add(R.id.fragment_container, f)
        ft.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            // Respond to the action bar's Up/Home button
            val upIntent: Intent? = NavUtils.getParentActivityIntent(this)
            if (upIntent == null) {
                throw IllegalStateException("No Parent Activity Intent")
            } else {
                val sm = CompressPhotosServiceManager.getInstance()
                if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
                    sm.updateJobStatus(JobStatus.Initialized)
                }
                NavUtils.navigateUpTo(this, upIntent)
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val sm = CompressPhotosServiceManager.getInstance()
        if (sm.jobStatus.value is JobStatus.Success || sm.jobStatus.value is JobStatus.Cancelled) {
            sm.updateJobStatus(JobStatus.Initialized)
        }
        super.onBackPressed()
    }

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, CompressPhotosActivity::class.java)
    }
}